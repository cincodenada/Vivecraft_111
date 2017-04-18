package com.mtbs3d.minecrift.asm;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Type;

import com.mtbs3d.minecrift.asm.handler.*;

import net.minecraft.entity.EntityLiving;
import net.minecraft.launchwrapper.IClassTransformer;

public class VivecraftASMTransformer implements IClassTransformer {
	private static final List<ASMClassHandler> asmHandlers = new ArrayList<ASMClassHandler>();
	static {
		asmHandlers.add(new ASMHandlerGuiContainer());
		asmHandlers.add(new ASMHandlerGuiContainerCreative());
		asmHandlers.add(new ASMHandlerGuiIngameForge());
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes) {
		for (ASMClassHandler handler : asmHandlers) {
			if (!handler.shouldPatchClass()) continue;
			ClassTuple tuple = handler.getDesiredClass();
			if (name.equals(tuple.classNameObf)) {
				System.out.println("Patching class: " + name + " (" + tuple.className + ")");
				bytes = handler.patchClass(bytes, true);
			} else if (name.equals(tuple.className)) {
				System.out.println("Patching class: " + name);
				bytes = handler.patchClass(bytes, false);
			}
		}
		return bytes;
	}
}
