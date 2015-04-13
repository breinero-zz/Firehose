package com.bryanreinero.firehose.schema.type;

import com.bryanreinero.firehose.schema.Type;

public abstract class StrictType implements Type {

	@Override
	public String getNamespace() {
		return "com.bryanreinero.strict";
	}
}
