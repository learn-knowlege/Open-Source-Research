    public void visitVarDef(JCVariableDecl tree) {
    	DEBUG.P(this,"visitVarDef(1)");
    	DEBUG.P("tree="+tree);
		DEBUG.P("env.info.scope鍓�"+env.info.scope);
    	DEBUG.P("env.info.scope.owner="+env.info.scope.owner);
		DEBUG.P("env.info.scope.owner.kind="+Kinds.toString(env.info.scope.owner.kind));

    	
        // Local variables have not been entered yet, so we need to do it now:
        if (env.info.scope.owner.kind == MTH) {
        	DEBUG.P("tree.sym="+tree.sym);
			/*鏂规硶鐨勫弬鏁板拰鏂规硶浣撲腑鐨勫眬閮ㄥ彉閲忓湪涓や釜浣滅敤鍩�Scope)涓紝濡�
			class Aclass<T> {
				void m(int a) {
					int b;
				}
			}
			env.info.scope=Scope[(entries=1 nelems=1 owner=m())b | (entries=1 nelems=1 owner=m())a | (entries=3 nelems=3 owner=Aclass)super, this, T]
			*/
            if (tree.sym != null) { //鏂规硶鍙傛暟宸插湪MemberEnter.signature(5)涓姞鍏�
                // parameters have already been entered
                env.info.scope.enter(tree.sym);
            } else {
                memberEnter.memberEnter(tree, env);
                annotate.flush();
            }
        }
		DEBUG.P("env.info.scope鍚�"+env.info.scope);
        
        DEBUG.P("chk.validate 鍓�);

        // Check that the variable's declared type is well-formed.
        chk.validate(tree.vartype);

        VarSymbol v = tree.sym;
        Lint lint = env.info.lint.augment(v.attributes_field, v.flags());
        Lint prevLint = chk.setLint(lint);

        try {
            chk.checkDeprecatedAnnotation(tree.pos(), v);
            
            DEBUG.P("tree.init="+tree.init);
            if (tree.init != null) {
                if ((v.flags_field & FINAL) != 0 && tree.init.tag != JCTree.NEWCLASS) {
                    // In this case, `v' is final.  Ensure that it's initializer is
                    // evaluated.
                    v.getConstValue(); // ensure initializer is evaluated
                } else {
                    // Attribute initializer in a new environment
                    // with the declared variable as owner.
                    // Check that initializer conforms to variable's declared type.
                    Env<AttrContext> initEnv = memberEnter.initEnv(tree, env);
                    initEnv.info.lint = lint;
                    // In order to catch self-references, we set the variable's
                    // declaration position to maximal possible value, effectively
                    // marking the variable as undefined.
                    v.pos = Position.MAXPOS;
                    attribExpr(tree.init, initEnv, v.type);
                    v.pos = tree.pos;
                }
            }
            result = tree.type = v.type;
            chk.validateAnnotations(tree.mods.annotations, v);
        }
        finally {
            chk.setLint(prevLint);
        }
        
        DEBUG.P(0,this,"visitVarDef(1)");
    }