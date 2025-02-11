import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// 语法分析类
public class yvfafenxi {
    // 输出结果列表
    public static ArrayList<String> answer = new ArrayList<>();

    // 当前索引
    public static int now_index = -1;

    // 当前行
    public static String now_line = "";

    // 上一行
    public static String pre_line = "";

    // 当前符号
    public static String now_fuhao = "";

    // 当前Token
    public static String now_token = "";

    // 局部符号表
    public static ArrayList<HashMap<String, fuhaobiao>> now_layer = new ArrayList<>();

    // 当前层次
    public static int now_layer_size = -1;

    // 规约产生式表
    public static ArrayList<ArrayList<Integer>> rp = new ArrayList<>();

    // 切分语法产生式
    public static ArrayList<Integer> cut_parse = new ArrayList<>();

    // 语法分析的入口，判断是否为CompUnit
    public static boolean CompUnit() {
        int re = getOne();

        // 解析全局变量和函数定义
        while (Decl(true)) {}

        while (FuncDef()) {}

        // 解析主函数定义
        if (!MainFuncDef()) {
            return false;
        }
        answer.add("<CompUnit>");
        return true;
    }

    // 解析主函数定义
    private static boolean MainFuncDef() {
        String mname;
        // 如果是int类型的主函数定义
        if (Objects.equals(now_fuhao, "INTTK")) {
            answer.add(now_fuhao + " " + now_token);
            int re = getOne();

            // 获取main函数的定义
            if (Objects.equals(now_fuhao, "MAINTK")) {
                mname = now_token;
                answer.add(now_fuhao + " " + now_token);
                re = getOne();

                // 将main函数添加到全局符号表
                fuhaobiao sb = new fuhaobiao(mname, -1, 2, 0);
                yingshe.globalFuncTable.put(mname, sb);
                HashMap<String, fuhaobiao> localTable = new HashMap<>();
                localTable.put(mname, sb);
                now_layer.add(localTable);
                now_layer_size++;

                // 解析main函数的参数
                if (Objects.equals(now_fuhao, "LPARENT")) {
                    answer.add(now_fuhao + " " + now_token);
                    re = getOne();
                    if (!Objects.equals(now_fuhao, "RPARENT")) {
                        System.out.println("函数缺少)！");
                        yingshe.error_output.set(Integer.parseInt(pre_line), pre_line + " " + "j");
                    } else {
                        answer.add(now_fuhao + " " + now_token);
                        re = getOne();
                    }
                    Block(0);
                }

                // 输出本层符号表
                System.out.println("本层符号表： ");
                for (String item : now_layer.get(now_layer_size).keySet()) {
                    System.out.println(item);
                }
                now_layer.remove(now_layer_size);
                now_layer_size--;
            }
        } else {
            System.out.println("main函数类型定义错误");
            return false;
        }
        answer.add("<MainFuncDef>");
        return true;
    }

    // 解析函数定义
    private static boolean FuncDef() {
        String fname;
        int pnum = 0;
        String funcline = "";
        int type = 0;

        // 如果是函数类型定义
        if (Objects.equals(now_fuhao, "VOIDTK") || Objects.equals(now_fuhao, "INTTK")) {
            if (Objects.equals(now_fuhao, "VOIDTK")) {
                type = 1;
            }
            int re = getOne();

            // 如果是main函数，回退
            if (Objects.equals(now_fuhao, "MAINTK")) {
                reTrack();
                return false;
            } else {
                reTrack();
                answer.add(now_fuhao + " " + now_token);
                answer.add("<FuncType>");
                re = getOne();

                // 获取函数名
                if (Objects.equals(now_fuhao, "IDENFR")) {
                    fname = now_token;
                    funcline = now_line;
                    answer.add(now_fuhao + " " + now_token);
                    re = getOne();

                    // 检查函数名是否与全局定义冲突
                    if (yingshe.globalFuncTable.containsKey(fname)) {
                        yingshe.error_output.set(Integer.parseInt(funcline), funcline + " " + "b");
                        System.out.println("与全局定义冲突！");
                    }

                    // 添加函数到全局符号表
                    fuhaobiao sb = new fuhaobiao(fname, -1, 2, type * 3);
                    yingshe.globalFuncTable.put(fname, sb);
                    HashMap<String, fuhaobiao> localTable = new HashMap<>();
                    localTable.put(fname, sb);
                    now_layer.add(localTable);
                    now_layer_size++;

                    // 解析函数参数
                    if (Objects.equals(now_fuhao, "LPARENT")) {
                        answer.add(now_fuhao + " " + now_token);
                        re = getOne();
                        FuncFParams(fname);

                        // 如果缺少右括号，报错
                        if (!Objects.equals(now_fuhao, "RPARENT")) {
                            System.out.println("函数缺少)！");
                            yingshe.error_output.set(Integer.parseInt(pre_line), pre_line + " " + "j");
                        } else {
                            answer.add(now_fuhao + " " + now_token);
                            re = getOne();
                        }

                        // 解析函数体
                        Block(type);

                        // 输出本层符号表
                        System.out.println("本层符号表： ");
                        for (String item : now_layer.get(now_layer_size).keySet()) {
                            System.out.println(item);
                        }
                        now_layer.remove(now_layer_size);
                        now_layer_size--;
                    } else {
                        System.out.println("函数缺少(！");
                        return false;
                    }
                } else {
                    System.out.println("函数缺少名称！");
                    return false;
                }
            }
        } else {
            System.out.println("函数开头错误！");
            return false;
        }
        answer.add("<FuncDef>");
        return true;
    }

    // 解析Block
    private static boolean Block(int type) {
        String rline = "";
        if (Objects.equals(now_fuhao, "LBRACE")) {
            answer.add(now_fuhao + " " + now_token);
            int re = getOne();

            // 如果是局部变量或者函数定义，添加局部符号表
            if (type == 21 || type == 20) {
                HashMap<String, fuhaobiao> localTable = new HashMap<>();
                now_layer.add(localTable);
                now_layer_size++;
            }

            // 解析Block内部语句
            while (!Objects.equals(now_fuhao, "RBRACE")) {
                BlockItem(type);
            }

            // 获取右花括号
            if (Objects.equals(now_fuhao, "RBRACE")) {
                rline = now_line;
                answer.add(now_fuhao + " " + now_token);
                re = getOne();

                // 如果存在多余的符号，报错
                if (re < 0) {
                    if (now_index < cifafenxi.output.size()) {
                        return false;
                    }
                }
            }

            // 如果是局部变量或者函数定义，输出本层符号表
            if (type == 21 || type == 20) {
                System.out.println("本层符号表： ");
                for (String item : now_layer.get(now_layer_size).keySet()) {
                    System.out.println(item);
                }
                now_layer.remove(now_layer_size);
                now_layer_size--;
            } else if (type == 0) {
                // 如果是int类型函数，检查是否缺少返回语句
                for (Map.Entry<String, fuhaobiao> entry : now_layer.get(now_layer_size).entrySet()) {
                    fuhaobiao funitem = entry.getValue();
                    System.out.println(entry.getKey());
                    if (funitem.kind == 2 && funitem.alreadyReturn == 0) {
                        System.out.println("int 类型函数最终缺少返回语句");
                        yingshe.error_output.set(Integer.parseInt(rline), rline + " " + "g");
                    }
                }
            }
        } else {
            System.out.println("Block没有使用{开头!");
            return false;
        }
        answer.add("<Block>");
        return true;
    }

    // 解析Block内部的语句项
    private static boolean BlockItem(int type) {

        // 尝试解析声明语句
        if (Decl(false)) {

        }
        // 尝试解析语句
        else if (Stmt(type)) {

        }
        // 如果既不是声明语句也不是普通语句，报错
        else {
            System.out.println("BlockItem 为空！");
            return false;
        }
        return true;
    }

    // 解析语句
    private static boolean Stmt(int type) {
        int next_type = 0;

        // 设置下一个语句的类型
        if (type == 0) {
            next_type = 20;
        } else if (type == 1) {
            next_type = 21;
        } else {
            next_type = type;
        }
        int re;

        // 解析if语句
        if (Objects.equals(now_fuhao, "IFTK")) {
            answer.add(now_fuhao + " " + now_token);
            re = getOne();

            // 判断if语句是否合法
            if (Objects.equals(now_fuhao, "LPARENT")) {
                answer.add(now_fuhao + " " + now_token);
                re = getOne();

                // 解析条件语句
                if (!Cond()) {
                    System.out.println("if 语句缺少Cond条件判断!");
                }

                // 判断是否有右括号
                if (!Objects.equals(now_fuhao, "RPARENT")) {
                    System.out.println("if 语句缺少)!");
                    yingshe.error_output.set(Integer.parseInt(pre_line), pre_line + " " + "j");
                } else {
                    answer.add(now_fuhao + " " + now_token);
                    re = getOne();
                }

                // 解析if语句的语句体
                if (!Stmt(next_type)) {
                    System.out.println("if中Stmt语句错误！");
                }

                // 判断是否有else语句
                if (Objects.equals(now_fuhao, "ELSETK")) {
                    answer.add(now_fuhao + " " + now_token);
                    re = getOne();

                    // 解析else语句的语句体
                    if (!Stmt(next_type)) {
                        System.out.println("if中Stmt语句错误！");
                    }
                }
            } else {
                System.out.println("if 语句缺少(!");
                return false;
            }
            answer.add("<Stmt>");
            return true;
        }

        // 解析while语句
        if (Objects.equals(now_fuhao, "WHILETK")) {
            answer.add(now_fuhao + " " + now_token);
            re = getOne();

            // 判断while语句是否合法
            if (Objects.equals(now_fuhao, "LPARENT")) {
                answer.add(now_fuhao + " " + now_token);
                re = getOne();

                // 解析条件语句
                if (!Cond()) {
                    System.out.println("while 语句缺少Cond条件判断!");
                }

                // 判断是否有右括号
                if (Objects.equals(now_fuhao, "RPARENT")) {
                    answer.add(now_fuhao + " " + now_token);
                    re = getOne();

                    // 标记语法产生式
                    cut_parse.add(1);

                    // 解析while语句的语句体
                    if (!Stmt(next_type)) {
                        System.out.println("while中Stmt语句错误！");
                    }

                    // 取消标记
                    cut_parse.remove(cut_parse.size() - 1);
                } else {
                    System.out.println("while 语句缺少)!");
                    yingshe.error_output.set(Integer.parseInt(pre_line), pre_line + " " + "j");
                }
            } else {
                System.out.println("while 语句缺少(!");
                return false;
            }
            answer.add("<Stmt>");
            return true;
        }

        // 解析break和continue语句
        if (Objects.equals(now_fuhao, "BREAKTK") || Objects.equals(now_fuhao, "CONTINUETK")) {
            String cutline = now_line;

            // 判断中断语句是否出现在合理位置
            if (cut_parse.isEmpty()) {
                System.out.println("中断语句出现位置不合理!");
                yingshe.error_output.set(Integer.parseInt(now_line), now_line + " " + "m");
            }

            answer.add(now_fuhao + " " + now_token);
            re = getOne();

            // 判断是否有分号
            if (Objects.equals(now_fuhao, "SEMICN")) {
                answer.add(now_fuhao + " " + now_token);
                re = getOne();
            } else {
                System.out.println("break|continue语句缺少;");
                yingshe.error_output.set(Integer.parseInt(cutline), cutline + " " + "i");
            }
            answer.add("<Stmt>");
            return true;
        }

        // 解析return语句
        if (Objects.equals(now_fuhao, "RETURNTK")) {
            String returnline = now_line;
            answer.add(now_fuhao + " " + now_token);
            re = getOne();

            // 如果存在返回值，解析表达式
            if (Exp()) {
                // 如果在void类型函数中出现return表达式，报错
                if (type == 1 || type == 21) {
                    System.out.println("无返回语句块中出现return表达式!");
                    yingshe.error_output.set(Integer.parseInt(returnline), returnline + " " + "f");
                }
                // 如果是全局层次的函数，标记已有返回语句
                else if (now_layer_size == 0) {
                    String symbol1 = now_fuhao;
                    re = getOne();
                    String symbol2 = now_fuhao;

                    // 如果是函数块的最后一行，标记函数已有返回语句
                    if (Objects.equals(symbol1, "RBRACE") || Objects.equals(symbol2, "RBRACE")) {
                        for (Map.Entry<String, fuhaobiao> entry : now_layer.get(now_layer_size).entrySet()) {
                            fuhaobiao funitem = entry.getValue();
                            if (funitem.kind == 2 && funitem.alreadyReturn == 0) {
                                funitem.alreadyReturn = 1;
                            }
                        }
                    }
                    reTrack();
                }
            }

            // 判断是否有分号
            if (Objects.equals(now_fuhao, "SEMICN")) {
                answer.add(now_fuhao + " " + now_token);
                re = getOne();
            } else {
                System.out.println("return语句缺少;");
                yingshe.error_output.set(Integer.parseInt(returnline), returnline + " " + "i");
            }
            answer.add("<Stmt>");
            return true;
        }

        // 解析printf语句
        if (Objects.equals(now_fuhao, "PRINTFTK")) {
            String pline = now_line;
            int fnum = 0;
            int pnum = -1;
            answer.add(now_fuhao + " " + now_token);
            re = getOne();

            // 判断是否有左括号
            if (Objects.equals(now_fuhao, "LPARENT")) {
                String lline = now_line;
                answer.add(now_fuhao + " " + now_token);
                re = getOne();

                // 判断是否有字符串常量
                if (Objects.equals(now_fuhao, "STRCON")) {
                    pnum = cifafenxi.formatNumber.get(Integer.parseInt(now_line));
                    answer.add(now_fuhao + " " + now_token);
                    re = getOne();
                }

                // 解析printf语句的参数
                while (Objects.equals(now_fuhao, "COMMA")) {
                    answer.add(now_fuhao + " " + now_token);
                    re = getOne();

                    // 解析表达式
                    if (!Exp()) {
                        System.out.println("printf输出语句缺少Exp！");
                    } else {
                        fnum++;
                    }
                }

                // 判断参数数量是否匹配
                if (pnum != fnum) {
                    System.out.println();
                    yingshe.error_output.set(Integer.parseInt(pline), pline + " " + "l");
                }

                // 判断是否有右括号
                if (Objects.equals(now_fuhao, "RPARENT")) {
                    answer.add(now_fuhao + " " + now_token);
                    re = getOne();

                    // 判断是否有分号
                    if (Objects.equals(now_fuhao, "SEMICN")) {
                        answer.add(now_fuhao + " " + now_token);
                        re = getOne();
                    } else {
                        System.out.println("printf语句缺少;");
                        yingshe.error_output.set(Integer.parseInt(pre_line), pre_line + " " + "i");
                    }
                } else {
                    System.out.println("printf语句缺少)!");
                    yingshe.error_output.set(Integer.parseInt(pre_line), pre_line + " " + "j");
                }
            } else {
                System.out.println("printf语句缺少(!");
                return false;
            }
            answer.add("<Stmt>");
            return true;
        }

        // 记录当前的词法和语法状态
        int pre_lexical = now_index;
        int pre_grammer = yvfafenxi.answer.size();
        int flag = 0;

        // 尝试解析左值（赋值语句的左侧）
        if (LVal()) {
            if (Objects.equals(now_fuhao, "ASSIGN")) {
                flag = 1;
            }
        }

        // 回退到初始状态
        now_index = pre_lexical;

        // 清除已经生成的语法项
        if (yvfafenxi.answer.size() > pre_grammer) {
            yvfafenxi.answer.subList(pre_grammer, yvfafenxi.answer.size()).clear();
        }

        // 回退一个字符
        reTrack();

        // 获取下一个字符
        getOne();

        // 如果是标识符，说明是赋值语句
        if (Objects.equals(now_fuhao, "IDENFR")) {
            String lvalName = now_token;
            re = getOne();

            // 如果是赋值语句
            if (flag == 1) {
                int kind = 0;
                int have = 0;

                // 判断左值是否为常量
                if (yingshe.globalTable.containsKey(lvalName)) {
                    kind = yingshe.globalTable.get(lvalName).kind;
                    have = 1;
                } else {
                    for (HashMap<String, fuhaobiao> map : now_layer) {
                        if (map.containsKey(lvalName)) {
                            have = 1;
                            kind = map.get(lvalName).kind;
                            break;
                        }
                    }
                }

                // 如果左值为常量，报错
                if (have != 0) {
                    if (kind == 1) {
                        System.out.println("这是个常量无法改变！");
                        yingshe.error_output.set(Integer.parseInt(pre_line), pre_line + " " + "h");
                    }
                }

                // 回退到初始状态
                reTrack();

                // 解析左值
                LVal();

                // 记录赋值符号
                answer.add(now_fuhao + " " + now_token);
                re = getOne();

                // 如果是getint语句
                if (Objects.equals(now_fuhao, "GETINTTK")) {
                    answer.add(now_fuhao + " " + now_token);
                    re = getOne();

                    // 判断是否有左括号
                    if (Objects.equals(now_fuhao, "LPARENT")) {
                        answer.add(now_fuhao + " " + now_token);
                        re = getOne();

                        // 判断是否有右括号
                        if (Objects.equals(now_fuhao, "RPARENT")) {
                            answer.add(now_fuhao + " " + now_token);
                            re = getOne();
                        } else {
                            System.out.println("getint缺少')'!");
                            yingshe.error_output.set(Integer.parseInt(pre_line), pre_line + " " + "j");
                        }
                    } else {
                        System.out.println("getint缺少'('!");
                        return false;
                    }
                }
                // 如果不是getint语句，解析表达式
                else if (!Exp()) {
                    System.out.println("LVal赋值语句缺少Exp！");
                    return false;
                }

                // 判断是否有分号
                if (Objects.equals(now_fuhao, "SEMICN")) {
                    answer.add(now_fuhao + " " + now_token);
                    re = getOne();
                } else {
                    System.out.println("LVal右边语句缺少;");
                    yingshe.error_output.set(Integer.parseInt(pre_line), pre_line + " " + "i");
                }
                answer.add("<Stmt>");
                return true;
            } else {
                // 回退到初始状态
                reTrack();
            }
        }

        // 记录当前的词法和语法状态
        pre_lexical = now_index;
        pre_grammer = yvfafenxi.answer.size();

        // 解析表达式语句
        if (Exp()) {
            if (Objects.equals(now_fuhao, "SEMICN")) {
                answer.add(now_fuhao + " " + now_token);
                if (getOne() < 0) return false;
            } else {
                System.out.println("EXP缺少;");
                yingshe.error_output.set(Integer.parseInt(pre_line), pre_line + " " + "i");
            }
            answer.add("<Stmt>");
            return true;
        } else {
            // 若表达式解析失败，则回退到之前状态
            now_index = pre_lexical;
            if (yvfafenxi.answer.size() > pre_grammer) {
                yvfafenxi.answer.subList(pre_grammer, yvfafenxi.answer.size()).clear();
            }
            reTrack();
            getOne();
            // 如果后面是分号，则表示为空语句，也算合法
            if (Objects.equals(now_fuhao, "SEMICN")) {
                answer.add(now_fuhao + " " + now_token);
                re = getOne();
                answer.add("<Stmt>");
                return true;
            }
        }

        // 解析复合语句
        if (Block(next_type)) {
            answer.add("<Stmt>");
            return true;
        }

        answer.add("<Stmt>");
        return true;
    }

    // 解析条件语句
    private static boolean Cond() {
        // 解析逻辑或表达式
        if (!LOrExp()) {
            System.out.println("Cond中没有LOrExp!");
            return false;
        }
        answer.add("<Cond>");
        return true;
    }

    // 解析逻辑或表达式
    private static boolean LOrExp() {
        if (LAndExp()) {
            while (Objects.equals(now_fuhao, "OR")) {
                answer.add("<LOrExp>");
                answer.add(now_fuhao + " " + now_token);
                if (getOne() < 0) return false;
                // 解析逻辑与表达式
                if (!LAndExp()) {
                    System.out.println("LOrExp当中||之后缺少表达式!");
                }
            }
        } else {
            System.out.println("不是LOrExp！");
            return false;
        }
        answer.add("<LOrExp>");
        return true;
    }

    // 解析逻辑与表达式
    private static boolean LAndExp() {
        if (EqExp()) {
            while (Objects.equals(now_fuhao, "AND")) {
                answer.add("<LAndExp>");
                answer.add(now_fuhao + " " + now_token);
                if (getOne() < 0) return false;
                // 解析相等表达式
                if (!EqExp()) {
                    System.out.println("LAndExp当中&&之后缺少表达式!");
                }
            }
        } else {
            System.out.println("不是LAndExp！");
            return false;
        }
        answer.add("<LAndExp>");
        return true;
    }

    // 解析相等表达式
    private static boolean EqExp() {
        if (RelExp()) {
            while (Objects.equals(now_fuhao, "EQL") || Objects.equals(now_fuhao, "NEQ")) {
                answer.add("<EqExp>");
                answer.add(now_fuhao + " " + now_token);
                if (getOne() < 0) return false;
                // 解析关系表达式
                if (!RelExp()) {
                    System.out.println("EqExp缺少右边RelExp表达式!");
                }
            }
        } else {
            System.out.println("不是EqExp！");
            return false;
        }
        answer.add("<EqExp>");
        return true;
    }

    // 解析关系表达式
    private static boolean RelExp() {
        if (AddExp()) {
            while (Objects.equals(now_fuhao, "LEQ")
                    || Objects.equals(now_fuhao, "LSS")
                    || Objects.equals(now_fuhao, "GEQ")
                    || Objects.equals(now_fuhao, "GRE")) {
                answer.add("<RelExp>");
                answer.add(now_fuhao + " " + now_token);
                if (getOne() < 0) return false;
                // 解析加法表达式
                if (!AddExp()) {
                    System.out.println("RelExp缺少右边AddExp表达式!");
                }
            }
        } else {
            System.out.println("不是RelExp!");
            return false;
        }
        answer.add("<RelExp>");
        return true;
    }

    // 解析函数实际参数列表
    private static int FuncRParams(String iname) {
        ArrayList<Integer> ar = new ArrayList<>();
        ar.add(-1);
        rp.add(ar);
        boolean flag;
        if (Exp()) {
            while (Objects.equals(now_fuhao, "COMMA")) {
                rp.get(rp.size() - 1).add(-1);
                answer.add(now_fuhao + " " + now_token);
                getOne();
                if (!Exp()) {
                    System.out.println("，后缺少函数参数！");
                }
            }
            answer.add("<FuncRParams>");
        }
        return 1;
    }

    // 解析函数形式参数列表
    private static boolean FuncFParams(String fname) {
        if (FuncFParam(fname)) {
            while (Objects.equals(now_fuhao, "COMMA")) {
                answer.add(now_fuhao + " " + now_token);
                if (getOne() < 0) return false;
                if (!FuncFParam(fname)) {
                    System.out.println("，后缺少函数参数！");
                    return false;
                }
            }
            answer.add("<FuncFParams>");
        } else {
            System.out.println("FuncFParams 无参数");
            return false;
        }
        return true;
    }

    // 解析函数形式参数
    private static boolean FuncFParam(String fname) {
        String fpname;
        int num = 0;
        int type;
        // 检查参数类型是否为INTTK
        if (Objects.equals(now_fuhao, "INTTK")) {
            answer.add(now_fuhao + " " + now_token);
            if (getOne() < 0) return false;
            // 检查参数标识符
            if (Objects.equals(now_fuhao, "IDENFR")) {
                fpname = now_token;
                answer.add(now_fuhao + " " + now_token);
                if (getOne() < 0) return false;
                int flag = 0;
                // 如果参数是数组
                if (Objects.equals(now_fuhao, "LBRACK")) {
                    String preline = now_line;
                    num += 1;
                    answer.add(now_fuhao + " " + now_token);
                    if (getOne() < 0) return false;
                    // 如果数组缺少右括号
                    if (!Objects.equals(now_fuhao, "RBRACK")) {
                        System.out.println("数组传值缺少]！");
                        yingshe.error_output.set(Integer.parseInt(preline), preline + " " + "k");
                    } else {
                        answer.add(now_fuhao + " " + now_token);
                        if (getOne() < 0) return false;
                    }
                    // 如果是二维数组
                    if (Objects.equals(now_fuhao, "LBRACK")) {
                        num += 1;
                        while (Objects.equals(now_fuhao, "LBRACK")) {
                            answer.add(now_fuhao + " " + now_token);
                            if (getOne() < 0) return false;
                            // 解析数组长度
                            if (!ConstExp()) {
                                System.out.println("二维数组第二维度缺少长度！");
                                return false;
                            }
                            // 如果数组缺少右括号
                            if (Objects.equals(now_fuhao, "RBRACK")) {
                                answer.add(now_fuhao + " " + now_token);
                                if (getOne() < 0) return false;
                            } else {
                                System.out.println("数组传值缺少]！");
                                yingshe.error_output.set(Integer.parseInt(preline), preline + " " + "k");
                                break;
                            }
                        }
                    }
                }
            } else {
                System.out.println("函数参数明缺失！");
                return false;
            }
        } else {
            System.out.println("函数参数类型错误！");
            return false;
        }
        // 添加参数到符号表
        int l = now_layer.size() - 1;
        if (!now_layer.get(l).containsKey(fpname)) {
            fuhaobiao sb = new fuhaobiao(fpname, -1, 0, num);
            now_layer.get(l).put(fpname, sb);
            yingshe.globalFuncTable.get(fname).insertPar(num);
        } else {
            yingshe.error_output.set(Integer.parseInt(now_line), now_line + " " + "b");
            System.out.println(fpname);
            System.out.println("函数定义参数与函数定义参数冲突！");
        }

        answer.add("<FuncFParam>");
        return true;
    }

    // 解析声明语句
    private static boolean Decl(boolean global) {
        int re;
        // 如果是const声明
        if (Objects.equals(now_fuhao, "CONSTTK")) {
            ConstDecl(global);
        }
        // 如果是int声明
        else if (Objects.equals(now_fuhao, "INTTK")) {
            re = getOne();
            re = getOne();
            // 如果是函数声明
            if (Objects.equals(now_fuhao, "LPARENT")) {
                reTrack();
                reTrack();
                return false;
            }
            // 如果是变量声明
            else {
                reTrack();
                reTrack();
                VarDecl(global);
            }
        } else {
            return false;
        }
        return true;
    }

    // 解析const声明
    private static boolean ConstDecl(boolean global) {
        if (Objects.equals(now_fuhao, "CONSTTK")) {
            answer.add(now_fuhao + " " + now_token);
            if (getOne() < 0) return false;
            // 检查是否是int类型
            if (Objects.equals(now_fuhao, "INTTK")) {
                answer.add(now_fuhao + " " + now_token);
                if (getOne() < 0) return false;
                // 解析const定义
                if (!ConstDef(global)) {}

                // 如果有逗号，继续解析下一个const定义
                if (Objects.equals(now_fuhao, "COMMA")) {
                    while (Objects.equals(now_fuhao, "COMMA")) {
                        answer.add(now_fuhao + " " + now_token);
                        if (getOne() < 0) return false;
                        if (!ConstDef(global)) {}
                    }
                }
                // 检查分号
                if (Objects.equals(now_fuhao, "SEMICN")) {
                    answer.add(now_fuhao + " " + now_token);
                    answer.add("<ConstDecl>");
                    if (getOne() < 0) return false;
                } else {
                    yingshe.error_output.set(Integer.parseInt(pre_line), pre_line + " " + "i");
                }
            }
        } else {
            return false;
        }
        return true;
    }

    // 解析变量声明
    private static boolean VarDecl(boolean global) {
        // 检查是否是int类型
        if (Objects.equals(now_fuhao, "INTTK")) {
            answer.add(now_fuhao + " " + now_token);
            if (getOne() < 0) return false;
            // 解析变量定义
            if (!VarDef(global)) {}

            // 如果有逗号，继续解析下一个变量定义
            if (Objects.equals(now_fuhao, "COMMA")) {
                while (Objects.equals(now_fuhao, "COMMA")) {
                    answer.add(now_fuhao + " " + now_token);
                    if (getOne() < 0) return false;
                    if (!VarDef(global)) {}
                }
            }
            // 检查分号
            if (Objects.equals(now_fuhao, "SEMICN")) {
                answer.add(now_fuhao + " " + now_token);
                answer.add("<VarDecl>");
                if (getOne() < 0) return false;
            } else {
                System.out.println("变量定义缺少;");
                yingshe.error_output.set(Integer.parseInt(pre_line), pre_line + " " + "i");
            }
        } else {
            return false;
        }
        return true;
    }

    // 解析变量定义
    private static boolean VarDef(boolean global) {
        String vname;
        int dem = 0;
        int re;
        String idline = "";
        // 检查变量标识符
        if (Objects.equals(now_fuhao, "IDENFR")) {
            idline = now_line;
            vname = now_token;
            answer.add(now_fuhao + " " + now_token);
            if (getOne() < 0) return false;
            // 如果是数组
            if (Objects.equals(now_fuhao, "LBRACK")) {
                while (Objects.equals(now_fuhao, "LBRACK")) {
                    System.out.println("1111111");
                    dem++;
                    answer.add(now_fuhao + " " + now_token);
                    if (getOne() < 0) return false;
                    // 解析数组长度
                    if (!ConstExp()) {
                        return false;
                    }
                    // 如果数组缺少右括号
                    if (Objects.equals(now_fuhao, "RBRACK")) {
                        answer.add(now_fuhao + " " + now_token);
                        if (getOne() < 0) return false;
                    } else {
                        yingshe.error_output.set(Integer.parseInt(now_line), now_line + " " + "k");
                        if (dem == 2) {
                            break;
                        }
                    }
                }
            }
            // 如果有赋值
            if (Objects.equals(now_fuhao, "ASSIGN")) {
                answer.add(now_fuhao + " " + now_token);
                if (getOne() < 0) return false;
                // 解析初始化值
                if (!InitVal()) {
                    reTrack();
                    return false;
                }
            }
            // 检查全局还是局部
            if (global) {
                // 检查是否和全局符号表冲突
                if (yingshe.globalTable.containsKey(vname)) {
                    yingshe.error_output.set(Integer.parseInt(idline), idline + " " + "b");
                    System.out.println("与全局定义冲突！");
                } else {
                    fuhaobiao sb = new fuhaobiao(vname, -1, 0, dem);
                    yingshe.globalTable.put(vname, sb);
                }
            } else {
                // 检查是否和局部符号表冲突
                HashMap<String, fuhaobiao> temp = now_layer.get(now_layer_size);
                if (temp.containsKey(vname)) {
                    if (temp.get(vname).kind != 2) {
                        yingshe.error_output.set(Integer.parseInt(idline), idline + " " + "b");
                        System.out.println("与局部定义冲突！");
                    }
                } else {
                    fuhaobiao sb = new fuhaobiao(vname, -1, 0, dem);
                    now_layer.get(now_layer_size).put(vname, sb);
                }
            }

            answer.add("<VarDef>");
            return true;
        } else {
            return false;
        }
    }

    // 解析常量定义
    private static boolean ConstDef(boolean global) {
        String cname;
        int dem = 0;
        int re;
        String constline = "";
        // 检查是否为标识符
        if (Objects.equals(now_fuhao, "IDENFR")) {
            constline = now_line;
            cname = now_token;
            answer.add(now_fuhao + " " + now_token);
            if (getOne() < 0) return false;
            // 如果是数组
            if (Objects.equals(now_fuhao, "LBRACK")) {
                while (Objects.equals(now_fuhao, "LBRACK")) {
                    dem++;
                    answer.add(now_fuhao + " " + now_token);
                    if (getOne() < 0) return false;
                    // 解析数组长度
                    if (!ConstExp()) {
                        return false;
                    }
                    // 如果数组缺少右括号
                    if (Objects.equals(now_fuhao, "RBRACK")) {
                        answer.add(now_fuhao + " " + now_token);
                        if (getOne() < 0) return false;
                    } else {
                        yingshe.error_output.set(Integer.parseInt(now_line), now_line + " " + "k");
                        if (dem == 2) {
                            break;
                        }
                    }
                }
            }
            // 如果有赋值
            if (Objects.equals(now_fuhao, "ASSIGN")) {
                answer.add(now_fuhao + " " + now_token);
                if (getOne() < 0) return false;
                // 解析初始化值
                if (!ConstInitVal()) {
                    return false;
                }
            } else {
                System.out.println("常量说明缺少等号！");
            }
            // 检查全局还是局部
            if (global) {
                // 检查是否和全局符号表冲突
                if (yingshe.globalTable.containsKey(cname)) {
                    yingshe.error_output.set(Integer.parseInt(constline), constline + " " + "b");
                    System.out.println("与全局定义冲突！");
                } else {
                    fuhaobiao sb = new fuhaobiao(cname, -1, 1, dem);
                    yingshe.globalTable.put(cname, sb);
                }
            } else {
                // 检查是否和局部符号表冲突
                HashMap<String, fuhaobiao> temp = now_layer.get(now_layer_size);
                if (temp.containsKey(cname)) {
                    if (temp.get(cname).kind != 2) {
                        yingshe.error_output.set(Integer.parseInt(constline), constline + " " + "b");
                        System.out.println("与局部定义冲突！");
                    }
                } else {
                    fuhaobiao sb = new fuhaobiao(cname, -1, 1, dem);
                    now_layer.get(now_layer_size).put(cname, sb);
                }
            }

            answer.add("<ConstDef>");
            return true;
        } else {
            return false;
        }
    }

    // 解析常量初始化值
    private static boolean ConstInitVal() {
        // 如果是左大括号
        if (Objects.equals(now_fuhao, "LBRACE")) {
            answer.add(now_fuhao + " " + now_token);
            if (getOne() < 0) return false;
            // 解析常量初始化值
            if (ConstInitVal()) {
                // 如果有逗号，继续解析下一个常量初始化值
                if (Objects.equals(now_fuhao, "COMMA")) {
                    while (Objects.equals(now_fuhao, "COMMA")) {
                        answer.add(now_fuhao + " " + now_token);
                        if (getOne() < 0) return false;
                        if (!ConstInitVal()) {
                            return false;
                        }
                    }
                }
            }
            // 检查右大括号
            if (Objects.equals(now_fuhao, "RBRACE")) {
                answer.add(now_fuhao + " " + now_token);
                if (getOne() < 0) return false;
            } else {
                System.out.println("ConstInitVal 缺少}!");
            }
        }
        // 如果是常量表达式
        else if (ConstExp()) {

        } else {
            return false;
        }

        answer.add("<ConstInitVal>");
        return true;
    }

    // 解析变量初始化值
    private static boolean InitVal() {
        // 如果是左大括号
        if (Objects.equals(now_fuhao, "LBRACE")) {
            answer.add(now_fuhao + " " + now_token);
            if (getOne() < 0) return false;
            // 解析变量初始化值
            if (InitVal()) {
                // 如果有逗号，继续解析下一个变量初始化值
                if (Objects.equals(now_fuhao, "COMMA")) {
                    while (Objects.equals(now_fuhao, "COMMA")) {
                        answer.add(now_fuhao + " " + now_token);
                        if (getOne() < 0) return false;
                        if (!InitVal()) {
                            return false;
                        }
                    }
                }
            }
            // 检查右大括号
            if (Objects.equals(now_fuhao, "RBRACE")) {
                answer.add(now_fuhao + " " + now_token);
                if (getOne() < 0) return false;
            } else {
                System.out.println("InitVal 缺少}!");
            }
        }
        // 如果不是左大括号，是表达式
        else if (!Exp()) {
            return false;
        }

        answer.add("<InitVal>");
        return true;
    }

    // 解析常量表达式
    private static boolean ConstExp() {
        // 解析加法表达式
        if (!AddExp()) {
            return false;
        }
        answer.add("<ConstExp>");
        return true;
    }

    // 解析加法表达式
    private static boolean AddExp() {
        // 如果是乘法表达式
        if (MulExp()) {
            while (Objects.equals(now_fuhao, "PLUS") || Objects.equals(now_fuhao, "MINU")) {
                answer.add("<AddExp>");
                answer.add(now_fuhao + " " + now_token);
                if (getOne() < 0) return false;
                // 如果是乘法表达式出错
                if (!MulExp()) {
                    System.out.println("Add Exp 第二种情况出现错误！");
                    return false;
                }
            }
        } else {
            return false;
        }
        answer.add("<AddExp>");
        return true;
    }

    /**
     * 判断是否为乘法表达式（MulExp）
     *
     * @return 若是乘法表达式，返回true；否则返回false。
     */
    private static boolean MulExp() {
        if (UnaryExp()) {
            // 判断是否为乘法、取余或除法
            if (Objects.equals(now_fuhao, "MULT")
                    || Objects.equals(now_fuhao, "MOD")
                    || Objects.equals(now_fuhao, "DIV")) {
                while (Objects.equals(now_fuhao, "MULT")
                        || Objects.equals(now_fuhao, "MOD")
                        || Objects.equals(now_fuhao, "DIV")) {
                    answer.add("<MulExp>");
                    answer.add(now_fuhao + " " + now_token);
                    if (getOne() < 0) return false;
                    if (!UnaryExp()) {
                        System.out.println("MulExp 第二种情况出现错误！");
                        return false;
                    }
                }
            }
        } else {
            return false;
        }

        answer.add("<MulExp>");
        return true;
    }

    /**
     * 判断是否为一元表达式（UnaryExp）
     *
     * @return 若是一元表达式，返回true；否则返回false。
     */
    private static boolean UnaryExp() {
        int flag = 0;
        String iname;
        String idenline = "";
        if (Objects.equals(now_fuhao, "IDENFR")) {
            iname = now_token;
            idenline = now_line;
            if (getOne() < 0) return false;
            // 如果不是左括号，回溯
            if (!Objects.equals(now_fuhao, "LPARENT")) {
                reTrack();
            } else {
                // 判断是否为全局函数
                if (!yingshe.globalFuncTable.containsKey(iname)) {
                    flag = 1;
                    yingshe.error_output.set(Integer.parseInt(idenline), idenline + " " + "c");
                    System.out.println("使用未定义名字！");
                } else {
                    reTrack();
                    answer.add(now_fuhao + " " + now_token);
                    if (getOne() < 0) return false;
                }
                // 如果没有错误标记，更新函数参数类型
                if (flag == 0) {
                    int type = yingshe.globalFuncTable.get(iname).type;
                    if (rp.size() != 0) {
                        ArrayList<Integer> a1 = rp.get(rp.size() - 1);
                        if (a1.get(a1.size() - 1) == -1) {
                            int l = rp.size();
                            rp.get(rp.size() - 1).set(a1.size() - 1, type);
                        }
                    }
                }

                // 如果是左括号，继续判断函数的实际参数
                if (Objects.equals(now_fuhao, "LPARENT")) {
                    answer.add(now_fuhao + " " + now_token);
                    if (getOne() < 0) return false;
                    FuncRParams(iname);

                    int l = rp.size();
                    // 如果没有错误标记，检查函数参数个数和类型是否匹配
                    if (flag == 0) {
                        ArrayList<Integer> fpt = yingshe.globalFuncTable.get(iname).parameterTable;
                        System.out.println(fpt.size());
                        for (int i : rp.get(l - 1)) {
                            System.out.println(i);
                        }
                        int psize = 0;
                        if (!rp.get(l - 1).contains(-1)) {
                            psize = rp.get(l - 1).size();
                        }
                        if (fpt.size() != psize) {
                            System.out.println("函数参数个数不匹配！");
                            yingshe.error_output.set(Integer.parseInt(idenline), idenline + " " + "d");
                        } else {
                            for (int i = 0; i < fpt.size(); i++) {
                                if (!Objects.equals(fpt.get(i), rp.get(l - 1).get(i))) {
                                    System.out.println("函数参数类型不匹配！");
                                    yingshe.error_output.set(Integer.parseInt(idenline), idenline + " " + "e");
                                }
                            }
                        }
                    }
                    rp.remove(l - 1);

                    // 判断是否有右括号
                    if (!Objects.equals(now_fuhao, "RPARENT")) {
                        System.out.println("UnaryExp中的func缺少)!");
                        yingshe.error_output.set(Integer.parseInt(now_line), now_line + " " + "j");
                    } else {
                        answer.add(now_fuhao + " " + now_token);
                        if (getOne() < 0) return false;
                    }
                    answer.add("<UnaryExp>");
                    return true;
                }
            }
        }

        // 如果不是标识符，判断是否是PrimaryExp或UnaryOp
        if (PrimaryExp()) {

        } else if (UnaryOp()) {
            if (!UnaryExp()) {
                return false;
            }
        } else {
            return false;
        }
        answer.add("<UnaryExp>");
        return true;
    }

    /**
     * 判断是否为一元操作符（UnaryOp）
     *
     * @return 若是一元操作符，返回true；否则返回false。
     */
    private static boolean UnaryOp() {
        if (Objects.equals(now_fuhao, "PLUS")) {
            answer.add(now_fuhao + " " + now_token);
            if (getOne() < 0) return false;
        } else if (Objects.equals(now_fuhao, "MINU")) {
            answer.add(now_fuhao + " " + now_token);
            if (getOne() < 0) return false;
        } else if (Objects.equals(now_fuhao, "NOT")) {
            answer.add(now_fuhao + " " + now_token);
            if (getOne() < 0) return false;
        } else {
            return false;
        }
        answer.add("<UnaryOp>");
        return true;
    }

    /**
     * 判断是否为基本表达式（PrimaryExp）
     *
     * @return 若是基本表达式，返回true；否则返回false。
     */
    private static boolean PrimaryExp() {
        if (Objects.equals(now_fuhao, "LPARENT")) {
            answer.add(now_fuhao + " " + now_token);
            if (getOne() < 0) return false;
            // 判断是否为表达式
            if (!Exp()) {
                System.out.println("PrimaryExp 当中的Exp错误！");
            }
            // 判断是否有右括号
            if (!Objects.equals(now_fuhao, "RPARENT")) {
                System.out.println("缺少右括号！");
                yingshe.error_output.set(Integer.parseInt(pre_line), pre_line + " " + "j");
            } else {
                answer.add(now_fuhao + " " + now_token);
                if (getOne() < 0) return false;
            }
        } else if (LVal()) {

        } else if (number()) {
            // 更新参数类型
            if (rp.size() != 0) {
                ArrayList<Integer> a1 = rp.get(rp.size() - 1);
                if (a1.get(a1.size() - 1) == -1) {
                    int l = rp.size();
                    rp.get(l - 1).set(a1.size() - 1, 0);
                }
            }
        } else {
            return false;
        }
        answer.add("<PrimaryExp>");
        return true;
    }

    /**
     * 判断是否为数字（number）
     *
     * @return 若是数字，返回true；否则返回false。
     */
    public static boolean number() {
        if (Objects.equals(now_fuhao, "INTCON")) {
            answer.add(now_fuhao + " " + now_token);
            getOne();
        } else {
            return false;
        }
        answer.add("<Number>");
        return true;
    }

    /**
     * 判断是否为左值（LVal）
     *
     * @return 若是左值，返回true；否则返回false。
     */
    private static boolean LVal() {
        String iname;
        int lnum = 0;
        int flag = 0;
        String lvalline = "";
        if (Objects.equals(now_fuhao, "IDENFR")) {
            iname = now_token;
            lvalline = now_line;
            answer.add(now_fuhao + " " + now_token);
            if (getOne() < 0) return false;
            if (rp.size() != 0) {
                ArrayList<Integer> a1 = rp.get(rp.size() - 1);
                if (a1.get(a1.size() - 1) == -1) {
                    rp.get(rp.size() - 1).set(a1.size() - 1, -2);
                    flag = 1;
                }
            }
        } else {
            return false;
        }

        if (Objects.equals(now_fuhao, "LPARENT")) {
            return false;
        }
        while (Objects.equals(now_fuhao, "LBRACK")) {
            lnum++;
            answer.add(now_fuhao + " " + now_token);
            if (getOne() < 0) return false;
            // 判断是否为表达式
            if (!Exp()) {
                System.out.println("Exp in LVal wrong!");
                return false;
            } else if (Objects.equals(now_fuhao, "RBRACK")) {
                answer.add(now_fuhao + " " + now_token);
                if (getOne() < 0) return false;
            } else if (!Objects.equals(now_fuhao, "RBRACK")) {
                System.out.println("LVal 缺少]!");
                yingshe.error_output.set(Integer.parseInt(now_line), now_line + " " + "k");
            }
        }
        int have = 0;
        int dnum = 0;
        // 判断是否在局部符号表中
        for (HashMap<String, fuhaobiao> map : now_layer) {
            if (map.containsKey(iname)) {
                have++;
                dnum = map.get(iname).type;
                break;
            }
        }
        if (have == 0) {
            // 判断是否在全局符号表中
            if (yingshe.globalTable.containsKey(iname)) {
                if (rp.size() != 0) {
                    ArrayList<Integer> a1 = rp.get(rp.size() - 1);
                    if (a1.get(a1.size() - 1) == -2) {
                        int dnum1 = yingshe.globalTable.get(iname).type;
                        int l = rp.size();
                        if ((dnum1 == 1 && lnum == 1) || (dnum1 == 2 && lnum == 2)) {
                            rp.get(l - 1).set(a1.size() - 1, 0);
                        } else if (dnum1 == 2 && lnum == 1) {
                            rp.get(l - 1).set(a1.size() - 1, 1);
                        } else {
                            rp.get(l - 1).set(a1.size() - 1, dnum1);
                        }
                    }
                }
            } else {
                yingshe.error_output.set(Integer.parseInt(lvalline), lvalline + " " + "c");
                System.out.println("使用未定义名字！");
            }
        } else {
            if (rp.size() != 0) {
                ArrayList<Integer> a1 = rp.get(rp.size() - 1);
                if (a1.get(a1.size() - 1) == -2 && flag == 1) {
                    int l = rp.size();
                    if ((dnum == 1 && lnum == 1) || (dnum == 2 && lnum == 2)) {
                        rp.get(l - 1).set(a1.size() - 1, 0);
                    } else if (dnum == 2 && lnum == 1) {
                        rp.get(l - 1).set(a1.size() - 1, 1);
                    } else {
                        rp.get(l - 1).set(a1.size() - 1, dnum);
                    }
                }
            }
        }
        answer.add("<LVal>");
        return true;
    }

    /**
     * 判断是否为表达式（Exp）
     *
     * @return 若是表达式，返回true；否则返回false。
     */
    private static boolean Exp() {
        if (!AddExp()) {
            return false;
        }
        answer.add("<Exp>");
        return true;
    }

    /**
     * 获取下一个符号，并更新全局符号、标识符、行数信息
     *
     * @return 若成功获取，返回1；否则返回-1。
     */
    public static int getOne() {
        now_index++;
        if (now_index >= cifafenxi.output.size()) {
            return -1;
        }
        pre_line = now_line;
        String s = cifafenxi.output.get(now_index);
        int space = s.indexOf(' ');
        int last = s.lastIndexOf(' ');
        now_fuhao = s.substring(0, space);
        now_token = s.substring(space + 1, last);
        now_line = s.substring(last + 1);
        return 1;
    }

    /** 回溯到上一个符号，并更新全局符号、标识符、行数信息 */
    public static void reTrack() {
        now_index--;
        pre_line = now_line;
        String s = cifafenxi.output.get(now_index);
        int space = s.indexOf(' ');
        int last = s.lastIndexOf(' ');
        now_fuhao = s.substring(0, space);
        now_token = s.substring(space + 1, last);
        now_line = s.substring(last + 1);
    }
}
