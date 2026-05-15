package io.github.lefraudeur;

import org.objectweb.asm.*;

import java.util.function.Consumer;

public final class Patcher {
    public static final int ASM_VERSION = Opcodes.ASM9;
    
    public static final String obf_net_minecraft_client_network_ClientCommonNetworkHandler_sendPacket = "method_52787";
    public static final String obf_net_minecraft_network_packet_Packet = "net/minecraft/class_2596";
    public static final String obf_net_minecraft_client_network_ClientCommonNetworkHandler = "net/minecraft/class_8673";
    public static final String obf_net_minecraft_client_MinecraftClient = "net/minecraft/class_310";
    public static final String obf_net_minecraft_client_MinecraftClient_tick = "method_1574";
    public static final String obf_net_minecraft_client_MinecraftClient_doAttack = "method_1536";

    public static final String obf_net_minecraft_entity_player_PlayerEntity = "net/minecraft/class_1657";
    public static final String obf_net_minecraft_entity_player_PlayerEntity_attack = "method_7324";
    public static final String obf_net_minecraft_entity_Entity = "net/minecraft/class_1297";

    public static final String obf_net_minecraft_network_ClientConnection = "net/minecraft/class_2535";
    public static final String obf_net_minecraft_network_ClientConnection_channelRead0 = "method_10770";
    public static final String io_netty_channel_ChannelHandlerContext = "io/netty/channel/ChannelHandlerContext";

    public static final String obf_net_minecraft_client_gui_hud_InGameHud = "net/minecraft/class_329";
    public static final String obf_net_minecraft_client_gui_hud_InGameHud_render = "method_1753";
    public static final String obf_net_minecraft_client_gui_DrawContext = "net/minecraft/class_332";

    public static final String obf_net_minecraft_client_render_GameRenderer = "net/minecraft/class_757";
    public static final String obf_net_minecraft_client_render_GameRenderer_updateTargetedEntity = "method_3190";

    public static final String obf_net_minecraft_block_AbstractBlock$AbstractBlockState = "net/minecraft/class_4970$class_4971";
    public static final String obf_net_minecraft_block_AbstractBlock$AbstractBlockState_getCollisionShape = "method_26194";
    public static final String obf_net_minecraft_world_BlockView = "net/minecraft/class_1922";
    public static final String obf_net_minecraft_util_math_BlockPos = "net/minecraft/class_2338";
    public static final String obf_net_minecraft_block_ShapeContext = "net/minecraft/class_3726";
    public static final String obf_net_minecraft_block_BlockState = "net/minecraft/class_2680";
    public static final String obf_net_minecraft_util_shape_VoxelShape = "net/minecraft/class_265";

    public static final String obf_net_minecraft_client_render_WorldRenderer = "net/minecraft/class_761";
    public static final String obf_net_minecraft_client_render_WorldRenderer_render = "method_22710";
    public static final String obf_net_minecraft_client_util_math_MatrixStack = "net/minecraft/class_4587";
    public static final String obf_net_minecraft_client_render_Camera = "net/minecraft/class_4184";
    public static final String obf_net_minecraft_client_render_LightmapTextureManager = "net/minecraft/class_765";
    public static final String obf_net_minecraft_client_render_RenderTickCounter = "net/minecraft/class_9779";
    public static final String obf_net_minecraft_client_util_ObjectAllocator = "net/minecraft/class_9922";
    public static final String com_mojang_blaze3d_buffers_GpuBufferSlice = "com/mojang/blaze3d/buffers/GpuBufferSlice";
    public static final String org_joml_Vector4f = "org/joml/Vector4f";
    public static final String org_joml_Matrix4f = "org/joml/Matrix4f";

    
    public static byte[] patch_net_minecraft_client_network_ClientCommonNetworkHandler(byte[] original_class) {
        ClassReader classReader = new ClassReader(original_class);
        ClassWriter classWriter = new SafeClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
        final int freeLocalIndexStart = MethodBasicInfoClassVisitor.getMaxLocals(classReader, 
                obf_net_minecraft_client_network_ClientCommonNetworkHandler_sendPacket, 
                "(L" + obf_net_minecraft_network_packet_Packet + ";)V");
        MethodModifier mod = new MethodModifier(
                obf_net_minecraft_client_network_ClientCommonNetworkHandler_sendPacket,
                "(L" + obf_net_minecraft_network_packet_Packet + ";)V",
                (MethodVisitor mv) -> {
                    int index = store_and_post_event(mv, "io.github.lefraudeur.events.PacketSendEvent",
                            freeLocalIndexStart,
                            obf_net_minecraft_client_network_ClientCommonNetworkHandler,
                            obf_net_minecraft_network_packet_Packet);
                    check_cancel_void(mv, index);
                },
                null,
                null);
        MethodModifier[] modifiers = { mod };
        MethodClassTransformer methodClassTransformer = new MethodClassTransformer(ASM_VERSION, classWriter,
                classReader, modifiers);
        classReader.accept(methodClassTransformer, 0);
        return classWriter.toByteArray();
    }

    public static byte[] patch_net_minecraft_client_MinecraftClient(byte[] original_class) {
        ClassReader classReader = new ClassReader(original_class);
        ClassWriter classWriter = new SafeClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
        final int tickLocals = MethodBasicInfoClassVisitor.getMaxLocals(classReader, obf_net_minecraft_client_MinecraftClient_tick, "()V");
        final int attackLocals = MethodBasicInfoClassVisitor.getMaxLocals(classReader, obf_net_minecraft_client_MinecraftClient_doAttack, "()Z");
        
        MethodModifier mod = new MethodModifier(
                obf_net_minecraft_client_MinecraftClient_tick,
                "()V",
                (MethodVisitor mv) -> {
                    int index = store_and_post_event(mv, "io.github.lefraudeur.events.PreTickEvent",
                            tickLocals,
                            obf_net_minecraft_client_MinecraftClient);
                    check_cancel_void(mv, index);
                },
                (MethodVisitor mv) -> {
                    store_and_post_event(mv, "io.github.lefraudeur.events.PostTickEvent",
                            tickLocals,
                            obf_net_minecraft_client_MinecraftClient);
                },
                null);
        MethodModifier mod2 = new MethodModifier(
                obf_net_minecraft_client_MinecraftClient_doAttack,
                "()Z",
                (MethodVisitor mv) -> {
                    int index = store_and_post_event(mv, "io.github.lefraudeur.events.PreDoAttackEvent",
                            attackLocals,
                            obf_net_minecraft_client_MinecraftClient);
                    check_cancel_boolean(mv, index);
                },
                (MethodVisitor mv) -> {
                    int index = store_and_post_event(mv, "io.github.lefraudeur.events.PostDoAttackEvent",
                            attackLocals,
                            obf_net_minecraft_client_MinecraftClient);
                    check_cancel_boolean(mv, index);
                },
                null);
        MethodModifier[] modifiers = { mod, mod2 };
        MethodClassTransformer methodClassTransformer = new MethodClassTransformer(ASM_VERSION, classWriter,
                classReader, modifiers);
        classReader.accept(methodClassTransformer, 0);
        return classWriter.toByteArray();
    }

    public static byte[] patch_net_minecraft_entity_player_PlayerEntity(byte[] original_class) {
        ClassReader classReader = new ClassReader(original_class);
        ClassWriter classWriter = new SafeClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
        final int freeLocalIndexStart = MethodBasicInfoClassVisitor.getMaxLocals(classReader, 
                obf_net_minecraft_entity_player_PlayerEntity_attack, 
                "(L" + obf_net_minecraft_entity_Entity + ";)V");
        MethodModifier mod = new MethodModifier(
                obf_net_minecraft_entity_player_PlayerEntity_attack,
                "(L" + obf_net_minecraft_entity_Entity + ";)V",
                (MethodVisitor mv) -> {
                    int index = store_and_post_event(mv, "io.github.lefraudeur.events.AttackEvent",
                            freeLocalIndexStart,
                            obf_net_minecraft_entity_player_PlayerEntity, obf_net_minecraft_entity_Entity);
                    check_cancel_void(mv, index);
                },
                null,
                null);
        MethodModifier[] modifiers = { mod };
        MethodClassTransformer methodClassTransformer = new MethodClassTransformer(ASM_VERSION, classWriter,
                classReader, modifiers);
        classReader.accept(methodClassTransformer, 0);
        return classWriter.toByteArray();
    }

    public static byte[] patch_net_minecraft_network_ClientConnection(byte[] original_class) {
        ClassReader classReader = new ClassReader(original_class);
        ClassWriter classWriter = new SafeClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
        final int freeLocalIndexStart = MethodBasicInfoClassVisitor.getMaxLocals(classReader,
                obf_net_minecraft_network_ClientConnection_channelRead0,
                "(L" + io_netty_channel_ChannelHandlerContext + ";L" + obf_net_minecraft_network_packet_Packet + ";)V");
        MethodModifier mod = new MethodModifier(
                obf_net_minecraft_network_ClientConnection_channelRead0,
                "(L" + io_netty_channel_ChannelHandlerContext + ";L" + obf_net_minecraft_network_packet_Packet + ";)V",
                (MethodVisitor mv) -> {
                    int index = store_and_post_event(mv, "io.github.lefraudeur.events.PacketReceiveEvent",
                            freeLocalIndexStart,
                            obf_net_minecraft_network_ClientConnection, io_netty_channel_ChannelHandlerContext,
                            obf_net_minecraft_network_packet_Packet);
                    check_cancel_void(mv, index);
                },
                null,
                null);
        MethodModifier[] modifiers = { mod };
        MethodClassTransformer methodClassTransformer = new MethodClassTransformer(ASM_VERSION, classWriter,
                classReader, modifiers);
        classReader.accept(methodClassTransformer, 0);
        return classWriter.toByteArray();
    }

    public static byte[] patch_net_minecraft_client_gui_hud_InGameHud(byte[] original_class) {
        ClassReader classReader = new ClassReader(original_class);
        ClassWriter classWriter = new SafeClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
        final String desc = "(L" + obf_net_minecraft_client_gui_DrawContext + ";L"
                + obf_net_minecraft_client_render_RenderTickCounter + ";)V";
        final int freeLocalIndexStart = MethodBasicInfoClassVisitor.getMaxLocals(classReader, 
                obf_net_minecraft_client_gui_hud_InGameHud_render, desc);
        MethodModifier mod = new MethodModifier(
                obf_net_minecraft_client_gui_hud_InGameHud_render,
                desc,
                (MethodVisitor mv) -> {
                    int index = store_and_post_event(mv, "io.github.lefraudeur.events.PreRender2DEvent",
                            freeLocalIndexStart,
                            obf_net_minecraft_client_gui_hud_InGameHud, obf_net_minecraft_client_gui_DrawContext,
                            obf_net_minecraft_client_render_RenderTickCounter);
                    check_cancel_void(mv, index);
                },
                (MethodVisitor mv) -> {
                    store_and_post_event(mv, "io.github.lefraudeur.events.PostRender2DEvent",
                            freeLocalIndexStart,
                            obf_net_minecraft_client_gui_hud_InGameHud, obf_net_minecraft_client_gui_DrawContext,
                            obf_net_minecraft_client_render_RenderTickCounter);
                },
                null);
        MethodModifier[] modifiers = { mod };
        MethodClassTransformer methodClassTransformer = new MethodClassTransformer(ASM_VERSION, classWriter,
                classReader, modifiers);
        classReader.accept(methodClassTransformer, 0);
        return classWriter.toByteArray();
    }

    public static byte[] patch_net_minecraft_client_render_GameRenderer(byte[] original_class) {
        ClassReader classReader = new ClassReader(original_class);
        ClassWriter classWriter = new SafeClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
        final int freeLocalIndexStart = MethodBasicInfoClassVisitor.getMaxLocals(classReader, 
                obf_net_minecraft_client_render_GameRenderer_updateTargetedEntity, "(F)V");
        MethodConstantModifier mod = new MethodConstantModifier(
                obf_net_minecraft_client_render_GameRenderer_updateTargetedEntity,
                "(F)V",
                9.0,
                (MethodVisitor mv) -> {
                    int index = store_and_post_event(mv, "io.github.lefraudeur.events.MidUpdateTargetedEntityEvent",
                            freeLocalIndexStart,
                            obf_net_minecraft_client_render_GameRenderer, "F_primitive");
                    invoke_void_event_method(mv, index, "getNewDoubleValue");
                    mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Double");
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D", false);
                });
        Modifier[] modifiers = { mod };
        MethodClassTransformer methodClassTransformer = new MethodClassTransformer(ASM_VERSION, classWriter,
                classReader, modifiers);
        classReader.accept(methodClassTransformer, 0);
        return classWriter.toByteArray();
    }

    public static byte[] patch_net_minecraft_world_BlockCollisionSpliterator(byte[] original_class) {
        ClassReader classReader = new ClassReader(original_class);
        ClassWriter classWriter = new SafeClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
        final int freeLocalIndexStart = MethodBasicInfoClassVisitor.getMaxLocals(classReader, "computeNext",
                "()Ljava/lang/Object;");
        final String targetMethodSig = "(L" + obf_net_minecraft_world_BlockView + ";L"
                + obf_net_minecraft_util_math_BlockPos + ";L" + obf_net_minecraft_block_ShapeContext + ";)L"
                + obf_net_minecraft_util_shape_VoxelShape + ";";
        MethodOnInvokeModifier mod = new MethodOnInvokeModifier(
                "computeNext",
                "()Ljava/lang/Object;",
                obf_net_minecraft_block_AbstractBlock$AbstractBlockState,
                obf_net_minecraft_block_AbstractBlock$AbstractBlockState_getCollisionShape,
                targetMethodSig,
                (MethodVisitor mv) -> {
                    int eventIndex = store_and_post_event_args_on_stack(mv,
                            "io.github.lefraudeur.events.BlockCollisionEvent", freeLocalIndexStart,
                            obf_net_minecraft_block_BlockState, obf_net_minecraft_world_BlockView,
                            obf_net_minecraft_util_math_BlockPos, obf_net_minecraft_block_ShapeContext);

                    Label continue_exec = new Label();
                    doIfCancelled(mv, eventIndex, () -> {
                        invoke_void_event_method(mv, eventIndex, "getReturnValue");
                        mv.visitTypeInsn(Opcodes.CHECKCAST, obf_net_minecraft_util_shape_VoxelShape);
                        mv.visitJumpInsn(Opcodes.GOTO, continue_exec);
                    });

                    invoke_void_event_method(mv, eventIndex, "getBlockState");
                    mv.visitTypeInsn(Opcodes.CHECKCAST, obf_net_minecraft_block_BlockState);
                    invoke_void_event_method(mv, eventIndex, "getWorld");
                    mv.visitTypeInsn(Opcodes.CHECKCAST, obf_net_minecraft_world_BlockView);
                    invoke_void_event_method(mv, eventIndex, "getPos");
                    mv.visitTypeInsn(Opcodes.CHECKCAST, obf_net_minecraft_util_math_BlockPos);
                    invoke_void_event_method(mv, eventIndex, "getContext");
                    mv.visitTypeInsn(Opcodes.CHECKCAST, obf_net_minecraft_block_ShapeContext);
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, obf_net_minecraft_block_AbstractBlock$AbstractBlockState,
                            obf_net_minecraft_block_AbstractBlock$AbstractBlockState_getCollisionShape, targetMethodSig,
                            false);

                    mv.visitLabel(continue_exec);
                });
        Modifier[] modifiers = { mod };
        MethodClassTransformer methodClassTransformer = new MethodClassTransformer(ASM_VERSION, classWriter,
                classReader, modifiers);
        classReader.accept(methodClassTransformer, 0);
        return classWriter.toByteArray();
    }

    public static byte[] patch_net_minecraft_client_render_WorldRenderer(byte[] original_class) {
        ClassReader classReader = new ClassReader(original_class);
        ClassWriter classWriter = new SafeClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
        final String desc = "(L" + obf_net_minecraft_client_util_ObjectAllocator + ";L"
                + obf_net_minecraft_client_render_RenderTickCounter + ";ZL"
                + obf_net_minecraft_client_render_Camera + ";L"
                + org_joml_Matrix4f + ";L"
                + org_joml_Matrix4f + ";L"
                + com_mojang_blaze3d_buffers_GpuBufferSlice + ";L"
                + org_joml_Vector4f + ";Z)V";
        final int freeLocalIndexStart = MethodBasicInfoClassVisitor.getMaxLocals(classReader, 
                obf_net_minecraft_client_render_WorldRenderer_render, desc);
        MethodModifier mod = new MethodModifier(
                obf_net_minecraft_client_render_WorldRenderer_render,
                desc,
                (MethodVisitor mv) -> {
                    int index = store_and_post_event(mv, "io.github.lefraudeur.events.PreRender3DEvent",
                            freeLocalIndexStart,
                            obf_net_minecraft_client_render_WorldRenderer,
                            obf_net_minecraft_client_util_ObjectAllocator,
                            obf_net_minecraft_client_render_RenderTickCounter,
                            "Z_primitive",
                            obf_net_minecraft_client_render_Camera,
                            org_joml_Matrix4f,
                            org_joml_Matrix4f,
                            com_mojang_blaze3d_buffers_GpuBufferSlice,
                            org_joml_Vector4f,
                            "Z_primitive");
                    check_cancel_void(mv, index);
                },
                null,
                (MethodVisitor mv) -> {
                    store_and_post_event(mv, "io.github.lefraudeur.events.PostRender3DEvent",
                            freeLocalIndexStart,
                            obf_net_minecraft_client_render_WorldRenderer,
                            obf_net_minecraft_client_util_ObjectAllocator,
                            obf_net_minecraft_client_render_RenderTickCounter,
                            "Z_primitive",
                            obf_net_minecraft_client_render_Camera,
                            org_joml_Matrix4f,
                            org_joml_Matrix4f,
                            com_mojang_blaze3d_buffers_GpuBufferSlice,
                            org_joml_Vector4f,
                            "Z_primitive");
                });
        MethodModifier[] modifiers = { mod };
        MethodClassTransformer methodClassTransformer = new MethodClassTransformer(ASM_VERSION, classWriter,
                classReader, modifiers);
        classReader.accept(methodClassTransformer, 0);
        return classWriter.toByteArray();
    }

    

    
    private static void push_class(MethodVisitor mv, String name, boolean use_other_cl) {
        if (use_other_cl) {
            mv.visitLdcInsn(name);
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/ClassLoader", "getSystemClassLoader",
                    "()Ljava/lang/ClassLoader;", false);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Class", "forName",
                    "(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;", false);
            return;
        }

        switch (name) {
            case "I_primitive" -> {
                mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;");
                return;
            }
            case "Z_primitive" -> {
                mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Boolean", "TYPE", "Ljava/lang/Class;");
                return;
            }
            case "J_primitive" -> {
                mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Long", "TYPE", "Ljava/lang/Class;");
                return;
            }
            case "F_primitive" -> {
                mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Float", "TYPE", "Ljava/lang/Class;");
                return;
            }
            case "D_primitive" -> {
                mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Double", "TYPE", "Ljava/lang/Class;");
                return;
            }
            case "S_primitive" -> {
                mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Short", "TYPE", "Ljava/lang/Class;");
                return;
            }
            case "B_primitive" -> {
                mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Byte", "TYPE", "Ljava/lang/Class;");
                return;
            }
            case "C_primitive" -> {
                mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Character", "TYPE", "Ljava/lang/Class;");
                return;
            }
        }
        mv.visitLdcInsn(name);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;",
                false);
    }

    private static void push_array_of_params(MethodVisitor mv, String... types) {
        push_array_of_params(mv, 0, types);
    }

    private static void push_array_of_params(MethodVisitor mv, int startIndex, String... types) {
        mv.visitLdcInsn(types.length);
        mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
        int param_index = startIndex;
        for (int i = 0; i < types.length; ++i) {
            mv.visitInsn(Opcodes.DUP);
            mv.visitLdcInsn(i);
            switch (types[i]) {
                case "S_primitive":
                    mv.visitTypeInsn(Opcodes.NEW, "java/lang/Short");
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitVarInsn(Opcodes.ILOAD, param_index);
                    mv.visitInsn(Opcodes.I2S);
                    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Short", "<init>", "(S)V", false);
                    break;
                case "Z_primitive":
                    mv.visitTypeInsn(Opcodes.NEW, "java/lang/Boolean");
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitVarInsn(Opcodes.ILOAD, param_index);
                    mv.visitInsn(Opcodes.I2B);
                    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Boolean", "<init>", "(Z)V", false);
                    break;
                case "I_primitive":
                    mv.visitTypeInsn(Opcodes.NEW, "java/lang/Integer");
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitVarInsn(Opcodes.ILOAD, param_index);
                    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Integer", "<init>", "(I)V", false);
                    break;
                case "J_primitive":
                    mv.visitTypeInsn(Opcodes.NEW, "java/lang/Long");
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitVarInsn(Opcodes.LLOAD, param_index);
                    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Long", "<init>", "(J)V", false);
                    ++param_index; 
                    break;
                case "F_primitive":
                    mv.visitTypeInsn(Opcodes.NEW, "java/lang/Float");
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitVarInsn(Opcodes.FLOAD, param_index);
                    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Float", "<init>", "(F)V", false);
                    break;
                case "D_primitive":
                    mv.visitTypeInsn(Opcodes.NEW, "java/lang/Double");
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitVarInsn(Opcodes.DLOAD, param_index);
                    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Double", "<init>", "(D)V", false);
                    ++param_index; 
                    break;
                default:
                    mv.visitVarInsn(Opcodes.ALOAD, param_index);
            }
            mv.visitInsn(Opcodes.AASTORE);
            ++param_index;
        }
    }

    private static void store_params_in_localvar_table(MethodVisitor mv, int freeLocalIndexStart, String... types) {
        for (int i = types.length - 1; i >= 0; i--) 
            mv.visitVarInsn(Opcodes.ASTORE, freeLocalIndexStart + i); 
    }

    private static void push_array_of_classes(MethodVisitor mv, String... class_names) {
        mv.visitLdcInsn(class_names.length);
        mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Class");
        for (int i = 0; i < class_names.length; ++i) {
            mv.visitInsn(Opcodes.DUP);
            mv.visitLdcInsn(i);
            push_class(mv, class_names[i].replace('/', '.'), false);
            mv.visitInsn(Opcodes.AASTORE);
        }
    }

    private static int store_and_post_event(MethodVisitor mv, String EventClassName, int freeLocalIndexStart, String... ConstructorClassNames) 
    {
        final int EventClass_index = freeLocalIndexStart;
        final int EventObject_index = freeLocalIndexStart + 1;

        push_class(mv, EventClassName.replace('/', '.'), true);
        mv.visitVarInsn(Opcodes.ASTORE, EventClass_index);

        mv.visitVarInsn(Opcodes.ALOAD, EventClass_index);
        push_array_of_classes(mv, ConstructorClassNames);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getConstructor",
                "([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;", false);

        push_array_of_params(mv, ConstructorClassNames);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/reflect/Constructor", "newInstance",
                "([Ljava/lang/Object;)Ljava/lang/Object;", false);
        mv.visitVarInsn(Opcodes.ASTORE, EventObject_index);

        mv.visitVarInsn(Opcodes.ALOAD, EventClass_index);
        mv.visitLdcInsn("dispatch");
        mv.visitInsn(Opcodes.ACONST_NULL);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getMethod",
                "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false);

        mv.visitVarInsn(Opcodes.ALOAD, EventObject_index);
        mv.visitInsn(Opcodes.ACONST_NULL);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke",
                "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", false);
        mv.visitInsn(Opcodes.POP);

        return EventObject_index;
    }

    private static int store_and_post_event_args_on_stack(MethodVisitor mv, String EventClassName,
            int freeLocalIndexStart, String... ConstructorClassNames) 
                                                                      
    {
        final int EventClass_index = freeLocalIndexStart;
        final int EventObject_index = freeLocalIndexStart + 1;
        
        push_class(mv, EventClassName.replace('/', '.'), true);
        mv.visitVarInsn(Opcodes.ASTORE, EventClass_index);

        mv.visitVarInsn(Opcodes.ALOAD, EventClass_index);
        push_array_of_classes(mv, ConstructorClassNames);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getConstructor",
                "([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;", false);

        push_array_of_params(mv, freeLocalIndexStart + 2, ConstructorClassNames);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/reflect/Constructor", "newInstance",
                "([Ljava/lang/Object;)Ljava/lang/Object;", false);
        mv.visitVarInsn(Opcodes.ASTORE, EventObject_index);

        mv.visitVarInsn(Opcodes.ALOAD, EventClass_index);
        mv.visitLdcInsn("dispatch");
        mv.visitInsn(Opcodes.ACONST_NULL);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getMethod",
                "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false);

        mv.visitVarInsn(Opcodes.ALOAD, EventObject_index);
        mv.visitInsn(Opcodes.ACONST_NULL);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke",
                "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", false);
        mv.visitInsn(Opcodes.POP);

        return EventObject_index;
    }

    private static void invoke_void_event_method(MethodVisitor mv, int eventIndex, String methodName) {
        mv.visitVarInsn(Opcodes.ALOAD, eventIndex);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
        mv.visitLdcInsn(methodName);
        mv.visitInsn(Opcodes.ACONST_NULL);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getMethod",
                "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false);

        mv.visitVarInsn(Opcodes.ALOAD, eventIndex);
        mv.visitInsn(Opcodes.ACONST_NULL);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke",
                "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", false);
    }

    private static void check_cancel_void(MethodVisitor mv, int eventIndex) {
        doIfCancelled(mv, eventIndex, () -> mv.visitInsn(Opcodes.RETURN));
    }

    private static void check_cancel_boolean(MethodVisitor mv, int eventIndex) {
        doIfCancelled(mv, eventIndex, () -> {
            invoke_void_event_method(mv, eventIndex, "getReturnValue");
            mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Boolean");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);

            mv.visitInsn(Opcodes.IRETURN);
        });
    }

    public static void doIfCancelled(MethodVisitor mv, int eventIndex, Runnable todo) {
        mv.visitVarInsn(Opcodes.ALOAD, eventIndex);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
        mv.visitLdcInsn("isCancelled");
        mv.visitInsn(Opcodes.ACONST_NULL);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getMethod",
                "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false);

        mv.visitVarInsn(Opcodes.ALOAD, eventIndex);
        mv.visitInsn(Opcodes.ACONST_NULL);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke",
                "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", false);
        mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Boolean");
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
        Label continue_exec = new Label();
        mv.visitJumpInsn(Opcodes.IFEQ, continue_exec);

        todo.run();

        mv.visitLabel(continue_exec);
    }

    private static class MethodBasicInfoClassVisitor extends ClassVisitor {
        private int returnCount;
        private int maxLocalsValue;
        private final String methodName;
        private final String methodSig;

        private MethodBasicInfoClassVisitor(int api, String methodName, String methodSig) {
            super(api);
            returnCount = 0;
            maxLocalsValue = 0;
            this.methodName = methodName;
            this.methodSig = methodSig;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                String[] exceptions) {
            if (name.equals(methodName) && descriptor.equals(methodSig)) {
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    public void visitInsn(int opcode) {
                        if (isReturn(opcode))
                            returnCount++;
                    }

                    @Override
                    public void visitMaxs(int maxStack, int maxLocals) {
                        maxLocalsValue = maxLocals;
                    }
                };
            }
            return null;
        }

        public static boolean isReturn(int opcode) {
            return opcode == Opcodes.RETURN
                    || opcode == Opcodes.ARETURN
                    || opcode == Opcodes.FRETURN
                    || opcode == Opcodes.DRETURN
                    || opcode == Opcodes.LRETURN
                    || opcode == Opcodes.IRETURN;
        }

        public static int getMethodReturnCount(ClassReader classReader, String methodName, String methodSig) {
            MethodBasicInfoClassVisitor classVisitor = new MethodBasicInfoClassVisitor(ASM_VERSION, methodName,
                    methodSig);
            classReader.accept(classVisitor, 0);
            return classVisitor.returnCount;
        }

        public static int getMaxLocals(ClassReader classReader, String methodName, String methodSig) {
            MethodBasicInfoClassVisitor classVisitor = new MethodBasicInfoClassVisitor(ASM_VERSION, methodName,
                    methodSig);
            classReader.accept(classVisitor, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            return classVisitor.maxLocalsValue;
        }
    }

    private static class SafeClassWriter extends ClassWriter {
        public SafeClassWriter(ClassReader classReader, int flags) {
            super(classReader, flags);
        }

        @Override
        protected String getCommonSuperClass(String type1, String type2) {
            try {
                return super.getCommonSuperClass(type1, type2);
            } catch (Exception e) {
                return "java/lang/Object";
            }
        }
    }

    private static class Modifier {
        private final String name;
        private final String descriptor;

        public Modifier(String name, String descriptor) {
            this.name = name;
            this.descriptor = descriptor;
        }
    }

    private static class MethodModifier extends Modifier {
        private final Consumer<MethodVisitor> atHead;
        private final Consumer<MethodVisitor> atTail;
        private final Consumer<MethodVisitor> atReturn;

        public MethodModifier(String name, String descriptor, Consumer<MethodVisitor> atHead,
                Consumer<MethodVisitor> atTail, Consumer<MethodVisitor> atReturn) {
            super(name, descriptor);
            this.atHead = atHead;
            this.atTail = atTail;
            this.atReturn = atReturn;
        }
    }

    private static class MethodOnInvokeModifier extends Modifier 
    {
        private final String targetOwner;
        private final String targetMethodName;
        private final String targetMethodSig;
        private final Consumer<MethodVisitor> onInvoke;

        public MethodOnInvokeModifier(String name, String descriptor, String targetOwner, String targetMethodName,
                String targetMethodSig, Consumer<MethodVisitor> onInvoke) {
            super(name, descriptor);
            this.targetOwner = targetOwner;
            this.targetMethodName = targetMethodName;
            this.targetMethodSig = targetMethodSig;
            this.onInvoke = onInvoke;
        }

    }

    private static class MethodConstantModifier extends Modifier {
        private final Object LDC;
        private final Consumer<MethodVisitor> atLDC;

        public MethodConstantModifier(String name, String descriptor, Object LDC, Consumer<MethodVisitor> atLDC) {
            super(name, descriptor);
            this.LDC = LDC;
            this.atLDC = atLDC;
        }
    }

    private static class MethodClassTransformer extends ClassVisitor {
        private final ClassReader classReader;
        private final Modifier[] modifiers;

        public MethodClassTransformer(int api, ClassVisitor classVisitor, ClassReader classReader,
                Modifier[] modifiers) {
            super(api, classVisitor);
            this.classReader = classReader;
            this.modifiers = modifiers;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                String[] exceptions) {
            for (Modifier modifier : modifiers) {
                if (!name.equals(modifier.name) || !descriptor.equals(modifier.descriptor))
                    continue;
                if (modifier instanceof MethodModifier methodModifier) {
                    int returnCount = (methodModifier.atTail != null
                            ? MethodBasicInfoClassVisitor.getMethodReturnCount(classReader, name, descriptor)
                            : 0);
                    return new MethodVisitor(api, cv.visitMethod(access, name, descriptor, signature, exceptions)) {
                        private int returnIndex = 0;

                        @Override
                        public void visitCode() {
                            mv.visitCode();
                            if (methodModifier.atHead != null)
                                methodModifier.atHead.accept(mv);
                        }

                        @Override
                        public void visitInsn(int opcode) {
                            if (MethodBasicInfoClassVisitor.isReturn(opcode)) {
                                if (methodModifier.atReturn != null)
                                    methodModifier.atReturn.accept(mv);
                                if (methodModifier.atTail != null) {
                                    returnIndex++;
                                    if (returnIndex == returnCount)
                                        methodModifier.atTail.accept(mv);
                                }
                            }
                            mv.visitInsn(opcode);
                        }
                    };
                } else if (modifier instanceof MethodConstantModifier constantModifier) {
                    return new MethodVisitor(api, cv.visitMethod(access, name, descriptor, signature, exceptions)) {
                        @Override
                        public void visitLdcInsn(Object value) {
                            if (!constantModifier.LDC.equals(value)) {
                                mv.visitLdcInsn(value);
                                return;
                            }
                            constantModifier.atLDC.accept(mv);
                        }
                    };
                } else if (modifier instanceof MethodOnInvokeModifier onInvokeModifier) {
                    return new MethodVisitor(api, cv.visitMethod(access, name, descriptor, signature, exceptions)) {
                        @Override
                        public void visitMethodInsn(int opcode, String owner, String name, String descriptor,
                                boolean isInterface) {
                            if (isInterface
                                    || opcode != Opcodes.INVOKEVIRTUAL
                                    || !name.equals(onInvokeModifier.targetMethodName)
                                    || !descriptor.equals(onInvokeModifier.targetMethodSig)) {
                                mv.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                                return;
                            }
                            onInvokeModifier.onInvoke.accept(mv);
                            mv.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                        }
                    };
                }
            }
            return cv.visitMethod(access, name, descriptor, signature, exceptions);
        }
    }
}
