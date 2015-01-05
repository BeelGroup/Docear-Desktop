/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2007 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.alibaba.fastjson.asm;

/**
 * A {@link MethodVisitor} that generates methods in bytecode form. Each visit method of this class appends the bytecode
 * corresponding to the visited instruction to a byte vector, in the order these methods are called.
 * 
 * @author Eric Bruneton
 * @author Eugene Kuleshov
 */
class MethodWriter implements MethodVisitor {

    /**
     * Pseudo access flag used to denote constructors.
     */
    static final int   ACC_CONSTRUCTOR                         = 262144;

    /**
     * Frame has exactly the same locals as the previous stack map frame and number of stack items is zero.
     */
    static final int   SAME_FRAME                              = 0;               // to 63 (0-3f)

    /**
     * Frame has exactly the same locals as the previous stack map frame and number of stack items is 1
     */
    static final int   SAME_LOCALS_1_STACK_ITEM_FRAME          = 64;              // to 127 (40-7f)

    /**
     * Reserved for future use
     */
    static final int   RESERVED                                = 128;

    /**
     * Frame has exactly the same locals as the previous stack map frame and number of stack items is 1. Offset is
     * bigger then 63;
     */
    static final int   SAME_LOCALS_1_STACK_ITEM_FRAME_EXTENDED = 247;             // f7

    /**
     * Frame where current locals are the same as the locals in the previous frame, except that the k last locals are
     * absent. The value of k is given by the formula 251-frame_type.
     */
    static final int   CHOP_FRAME                              = 248;             // to 250 (f8-fA)

    /**
     * Frame has exactly the same locals as the previous stack map frame and number of stack items is zero. Offset is
     * bigger then 63;
     */
    static final int   SAME_FRAME_EXTENDED                     = 251;             // fb

    /**
     * Frame where current locals are the same as the locals in the previous frame, except that k additional locals are
     * defined. The value of k is given by the formula frame_type-251.
     */
    static final int   APPEND_FRAME                            = 252;             // to 254 // fc-fe

    /**
     * Full frame
     */
    static final int   FULL_FRAME                              = 255;             // ff

    /**
     * Next method writer (see {@link ClassWriter#firstMethod firstMethod}).
     */
    MethodWriter       next;

    /**
     * The class writer to which this method must be added.
     */
    final ClassWriter  cw;

    /**
     * Access flags of this method.
     */
    private int        access;

    /**
     * The index of the constant pool item that contains the name of this method.
     */
    private final int  name;

    /**
     * The index of the constant pool item that contains the descriptor of this method.
     */
    private final int  desc;

    /**
     * If not zero, indicates that the code of this method must be copied from the ClassReader associated to this writer
     * in <code>cw.cr</code>. More precisely, this field gives the number of bytes to copied from <code>cw.cr.b</code>.
     */
    int                classReaderLength;

    /**
     * Number of exceptions that can be thrown by this method.
     */
    int                exceptionCount;

    /**
     * The exceptions that can be thrown by this method. More precisely, this array contains the indexes of the constant
     * pool items that contain the internal names of these exception classes.
     */
    int[]              exceptions;

    /**
     * The bytecode of this method.
     */
    private ByteVector code                                    = new ByteVector();

    /**
     * Maximum stack size of this method.
     */
    private int        maxStack;

    /**
     * Maximum number of local variables for this method.
     */
    private int        maxLocals;

    // ------------------------------------------------------------------------

    /*
     * Fields for the control flow graph analysis algorithm (used to compute the maximum stack size). A control flow
     * graph contains one node per "basic block", and one edge per "jump" from one basic block to another. Each node
     * (i.e., each basic block) is represented by the Label object that corresponds to the first instruction of this
     * basic block. Each node also stores the list of its successors in the graph, as a linked list of Edge objects.
     */

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructs a new {@link MethodWriter}.
     * 
     * @param cw the class writer in which the method must be added.
     * @param access the method's access flags (see {@link Opcodes}).
     * @param name the method's name.
     * @param desc the method's descriptor (see {@link Type}).
     * @param signature the method's signature. May be <tt>null</tt>.
     * @param exceptions the internal names of the method's exceptions. May be <tt>null</tt>.
     * @param computeMaxs <tt>true</tt> if the maximum stack size and number of local variables must be automatically
     * computed.
     * @param computeFrames <tt>true</tt> if the stack map tables must be recomputed from scratch.
     */
    MethodWriter(final ClassWriter cw, final int access, final String name, final String desc, final String signature, final String[] exceptions){
        if (cw.firstMethod == null) {
            cw.firstMethod = this;
        } else {
            cw.lastMethod.next = this;
        }
        cw.lastMethod = this;
        this.cw = cw;
        this.access = access;
        this.name = cw.newUTF8(name);
        this.desc = cw.newUTF8(desc);

        if (exceptions != null && exceptions.length > 0) {
            exceptionCount = exceptions.length;
            this.exceptions = new int[exceptionCount];
            for (int i = 0; i < exceptionCount; ++i) {
                this.exceptions[i] = cw.newClass(exceptions[i]);
            }
        }
    }

    // ------------------------------------------------------------------------
    // Implementation of the MethodVisitor interface
    // ------------------------------------------------------------------------

    public void visitInsn(final int opcode) {
        // adds the instruction to the bytecode of the method
        code.putByte(opcode);
        // update currentBlock
        // Label currentBlock = this.currentBlock;
    }

    public void visitIntInsn(final int opcode, final int operand) {
        // Label currentBlock = this.currentBlock;
        // adds the instruction to the bytecode of the method
        // if (opcode == Opcodes.SIPUSH) {
        // code.put12(opcode, operand);
        // } else { // BIPUSH or NEWARRAY
        code.put11(opcode, operand);
        // }
    }

    public void visitVarInsn(final int opcode, final int var) {
        // Label currentBlock = this.currentBlock;
        // adds the instruction to the bytecode of the method
        if (var < 4 && opcode != Opcodes.RET) {
            int opt;
            if (opcode < Opcodes.ISTORE) {
                /* ILOAD_0 */
                opt = 26 + ((opcode - Opcodes.ILOAD) << 2) + var;
            } else {
                /* ISTORE_0 */
                opt = 59 + ((opcode - Opcodes.ISTORE) << 2) + var;
            }
            code.putByte(opt);
        } else if (var >= 256) {
            code.putByte(196 /* WIDE */).put12(opcode, var);
        } else {
            code.put11(opcode, var);
        }
    }

    public void visitTypeInsn(final int opcode, final String type) {
        Item i = cw.newClassItem(type);
        // Label currentBlock = this.currentBlock;
        // adds the instruction to the bytecode of the method
        code.put12(opcode, i.index);
    }

    public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
        Item i = cw.newFieldItem(owner, name, desc);
        // Label currentBlock = this.currentBlock;
        // adds the instruction to the bytecode of the method
        code.put12(opcode, i.index);
    }

    public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc) {
        boolean itf = opcode == Opcodes.INVOKEINTERFACE;
        Item i = cw.newMethodItem(owner, name, desc, itf);
        int argSize = i.intVal;
        // Label currentBlock = this.currentBlock;
        // adds the instruction to the bytecode of the method
        if (itf) {
            if (argSize == 0) {
                argSize = Type.getArgumentsAndReturnSizes(desc);
                i.intVal = argSize;
            }
            code.put12(Opcodes.INVOKEINTERFACE, i.index).put11(argSize >> 2, 0);
        } else {
            code.put12(opcode, i.index);
        }
    }

    public void visitJumpInsn(final int opcode, final Label label) {
        // Label currentBlock = this.currentBlock;
        // adds the instruction to the bytecode of the method
        if ((label.status & Label.RESOLVED) != 0 && label.position - code.length < Short.MIN_VALUE) {
            throw new UnsupportedOperationException();
        } else {
            /*
             * case of a backward jump with an offset >= -32768, or of a forward jump with, of course, an unknown
             * offset. In these cases we store the offset in 2 bytes (which will be increased in resizeInstructions, if
             * needed).
             */
            code.putByte(opcode);
            label.put(this, code, code.length - 1);
        }
    }

    public void visitLabel(final Label label) {
        // resolves previous forward references to label, if any
        label.resolve(this, code.length, code.data);
    }

    public void visitLdcInsn(final Object cst) {
        Item i = cw.newConstItem(cst);
        // Label currentBlock = this.currentBlock;
        // adds the instruction to the bytecode of the method
        int index = i.index;
        if (i.type == ClassWriter.LONG || i.type == ClassWriter.DOUBLE) {
            code.put12(20 /* LDC2_W */, index);
        } else if (index >= 256) {
            code.put12(19 /* LDC_W */, index);
        } else {
            code.put11(Opcodes.LDC, index);
        }
    }

    public void visitIincInsn(final int var, final int increment) {
        // adds the instruction to the bytecode of the method
//        if ((var > 255) || (increment > 127) || (increment < -128)) {
//            code.putByte(196 /* WIDE */).put12(Opcodes.IINC, var).putShort(increment);
//        } else {
            code.putByte(Opcodes.IINC).put11(var, increment);
//        }
    }

    public void visitMaxs(final int maxStack, final int maxLocals) {
        this.maxStack = maxStack;
        this.maxLocals = maxLocals;
    }

    public void visitEnd() {
    }

    // ------------------------------------------------------------------------
    // Utility methods: control flow analysis algorithm
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Utility methods: stack map frames
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Utility methods: dump bytecode array
    // ------------------------------------------------------------------------

    /**
     * Returns the size of the bytecode of this method.
     * 
     * @return the size of the bytecode of this method.
     */
    final int getSize() {
        int size = 8;
        if (code.length > 0) {
            cw.newUTF8("Code");
            size += 18 + code.length + 8 * 0;
        }
        if (exceptionCount > 0) {
            cw.newUTF8("Exceptions");
            size += 8 + 2 * exceptionCount;
        }
        return size;
    }

    /**
     * Puts the bytecode of this method in the given byte vector.
     * 
     * @param out the byte vector into which the bytecode of this method must be copied.
     */
    final void put(final ByteVector out) {
        int mask = Opcodes.ACC_DEPRECATED | ClassWriter.ACC_SYNTHETIC_ATTRIBUTE | ((access & ClassWriter.ACC_SYNTHETIC_ATTRIBUTE) / (ClassWriter.ACC_SYNTHETIC_ATTRIBUTE / Opcodes.ACC_SYNTHETIC));
        out.putShort(access & ~mask).putShort(name).putShort(desc);
        int attributeCount = 0;
        if (code.length > 0) {
            ++attributeCount;
        }
        if (exceptionCount > 0) {
            ++attributeCount;
        }

        out.putShort(attributeCount);
        if (code.length > 0) {
            int size = 12 + code.length + 8 * 0; // handlerCount
            out.putShort(cw.newUTF8("Code")).putInt(size);
            out.putShort(maxStack).putShort(maxLocals);
            out.putInt(code.length).putByteArray(code.data, 0, code.length);
            out.putShort(0); // handlerCount
            attributeCount = 0;
            out.putShort(attributeCount);
        }
        if (exceptionCount > 0) {
            out.putShort(cw.newUTF8("Exceptions")).putInt(2 * exceptionCount + 2);
            out.putShort(exceptionCount);
            for (int i = 0; i < exceptionCount; ++i) {
                out.putShort(exceptions[i]);
            }
        }

    }

}
