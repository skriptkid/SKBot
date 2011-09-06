package org.rsbot.loader.script.adapter;

import org.rsbot.loader.asm.ClassAdapter;
import org.rsbot.loader.asm.ClassVisitor;

public class AddFieldAdapter extends ClassAdapter {
	public static class Field {
		public int access;
		public String name;
		public String desc;
	}

	private final Field[] fields;

	public AddFieldAdapter(final ClassVisitor delegate, final Field[] fields) {
		super(delegate);
		this.fields = fields;
	}

	@Override
	public void visitEnd() {
		for (final Field f : fields) {
			cv.visitField(f.access, f.name, f.desc, null, null);
		}
		cv.visitEnd();
	}
}
