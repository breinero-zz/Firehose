package com.bryanreinero.firehose.dao.mongo;

import com.bryanreinero.firehose.metrics.Interval;
import com.bryanreinero.firehose.util.Operation;
import com.bryanreinero.firehose.util.Result;
import com.mongodb.*;
import org.bson.BsonDocument;
import org.bson.Document;

/**
 * Created by breinero on 10/11/15.
 */
public class Insert extends Operation {

    private Document document;
    private final MongoDAO descriptor;

    public Insert(Document document, MongoDAO descriptor ) {
        super( descriptor );
        this.descriptor = descriptor;
        this.document = document;
    }

    @Override
    public Result call() throws Exception {
        Result r = new Result( false );
        incAttempts();

         try ( Interval i = descriptor.getSamples().set( getName() ) ) {
            descriptor.getCollection().insertOne( document );
        }
        catch ( MongoWriteException mwe ) {
            WriteError error = mwe.getError();

            if( error.getCategory().equals( ErrorCategory.DUPLICATE_KEY ) ) {
                r.setFailed( "Duplicate key error "+error.getMessage() );
            }

            if (error.getCategory().equals(ErrorCategory.EXECUTION_TIMEOUT)) {
                // May be eligible for a retry
                AttemptRetry();
            }

            if (error.getCategory().equals( ErrorCategory.UNCATEGORIZED ) )
                AttemptRetry();


        } catch (MongoWriteConcernException mwce) {
            com.mongodb.bulk.WriteConcernError error;
            error = mwce.getWriteConcernError();
            BsonDocument details = error.getDetails();
            r.setFailed( "Operation failed. "+details.toString() );

        } catch ( MongoTimeoutException mte ) {
            AttemptRetry();
        } catch (MongoException me) {
            r.setFailed( "Operation failed. "+me.toString());
        }

        return r;
    }
}