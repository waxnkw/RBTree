import java.io.*;

/*
 * 输入的语法是：
 * RBTree ::== 	   '(' RBTree ',' '(' Integer, COLOR ')' ',' RBTree')'
 * 				|  "NIL"
 * COLOR ::= Black | Red
 *
 * 其中的key值都是非负整数。
 */

public class RBTree {
    static final String inputPath = "inputs/rb0.in";
    static final String outputPath = "outputs/rb0.out";

    static final boolean LGN = false;

    static final int RED = 0;
    static final int BLACK = 1;

    static final int INSERT = 3;
    static final int DELETE = 4;

    int key;
    RBTree left, right;
    int color; //0--red, 1--black


    public RBTree(RBTree l, int k, int c, RBTree r) {
        this.left = l;
        this.key = k;
        this.color = c;
        this.right = r;
    }


    static String[] testCases = {
            "(NIL, (1,black), NIL)",
            "((NIL, (1,red), NIL), (5,black), (NIL, (7,red), NIL))",
            "(NIL, (5,black), (NIL, (7,red), NIL))",
            "(NIL, (5,black), (NIL, (4,red), NIL))",
            "((NIL, (1,black), NIL), (5,black), (NIL, (7,black), NIL))",
            "((NIL, (1,black), NIL), (5,red), (NIL, (7,black), NIL))",
            "(((NIL, (1,black), NIL), (5,red), (NIL, (7,black), NIL)), (10, black), (NIL, (12,black), NIL))"
    };
    /*
     * 测试
     * 从testCases数组中获得各个测试用例，扫描得到RBT，然后检查BlackHeight约束和Color约束，如果都通过了就输出
     */
    public static void main(final String[] args) {
        long startTime=System.currentTimeMillis(); //获取结束时间

        RBTree rbt = null;
        String [] cmds = getLinesFromFile(inputPath);

        File outFile = new File(outputPath);
        try {
            FileWriter writer = new FileWriter(outFile);

            for(int i=0; i<cmds.length; i++)
            {
                String s = cmds[i];
                if (s == null || s.length()==0){break;}
                int mode = RBTree.analysisCommand(s);
                int val = RBTree.getVal(s);
                if (mode==RBTree.INSERT){
                    rbt = RBTree.rbsInsert(rbt,val).newTree;
                }
                else if (mode==RBTree.DELETE){
                    rbt = RBTree.rbtDelete(rbt,val);
                }
                else {System.out.println("error");}
                rbt.color = RBTree.BLACK;

                //判断时间时不写文件
                if (LGN){continue;}

                writer.write(rbt.toString()+"\n");
            }
            if (!LGN){writer.flush();}
            else {System.out.println(rbt.toString());}

        } catch (IOException e) {
            e.printStackTrace();
        }

        long endTime=System.currentTimeMillis(); //获取结束时间
        if (LGN){System.out.println("程序运行时间： "+(endTime-startTime)+"ms");}
    }

    /****************************************************************************************************/
    //　　下　　　　　　　　　　　　　　　　　　我的代码区域
    /****************************************************************************************************/
    static String[] getLinesFromFile(String path){
        File file = new File(path);
        BufferedReader reader = null;
        String [] ret = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String num = reader.readLine();
            int n = Integer.parseInt(num);
            ret= new String[n];
            for (int i=0; i<n; i++){
                ret[i] = reader.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * 分析命令模式
     * */
    static int analysisCommand(String command){
        String [] ss = command.split(" ");
        if (ss[0].equals("a")){return INSERT;}
        if (ss[0].equals("d")){return DELETE;}
        return -1;
    }
    /**
     * 得到当前的需要操作的值
     * */
    static int getVal(String command){
        return Integer.parseInt(command.split(" ")[1]);
    }

    /**
     * @param rbt 当前的红黑树
     * 输出　树　元素个数　黑高度
     * 检查　颜色限制　和　高度限制
     * */
    static boolean checkRBTree(RBTree rbt){
        if (rbt==null){
            System.out.println("empty tree");
            return true;
        }

//        System.out.println("Output\t" + rbt.toString());

        int h = rbt.getBlackHeight();
        boolean colorCheck = rbt.checkColor();

//        System.out.println("\tColor constraint " + (colorCheck ? "OK" : "broken"));
//        System.out.println("\tBlack height " + (h >= 0 ? " is " + h : "constraint broken"));
//        System.out.print("\tElements in tree:");
//        RBTree.outputElements_Rec(rbt);
        boolean orderCheck = RBTree.checkOrder(rbt);
//        System.out.println("\n\tOrder constraint " + (orderCheck ? "OK" : "broken"));
//        System.out.println();
        return orderCheck&colorCheck;
    }

    /**********************   insert    *********************************/
    static class InsReturn{
        public RBTree newTree;
        public int status;
    }

    static final int ok = 0;
    static final int rbr = 1;
    static final int brb = 2;
    static final int rrb = 3;
    static final int brr = 4;

    /**
     * insert
     * @param
     * */
    static InsReturn rbsInsert(RBTree oldTree, int newNode){
        InsReturn ans, ansLeft, ansRight;
        if (oldTree==null){
            ans = new InsReturn();
            RBTree rbt = new RBTree(null, newNode, RED, null);
            ans.newTree = rbt;
            ans.status = brb;
        }
        else {
            if (newNode<oldTree.key){
                ansLeft = rbsInsert(oldTree.left, newNode);
                ans = repairLeft(oldTree, ansLeft);
            }
            else {
                ansRight = rbsInsert(oldTree.right, newNode);
                ans = repairRight(oldTree, ansRight);
            }
        }
        return ans;
    }

    /**
     * repair left
     * */
    static InsReturn repairLeft(RBTree oldTree, InsReturn ansLeft){
        InsReturn ans = new InsReturn();
        if (ansLeft.status == ok){
            ans.newTree = oldTree;
            ans.status = ok;
        }
        else{
            oldTree.left = ansLeft.newTree;
            if (ansLeft.status == rbr){
                ans.newTree = oldTree;
                ans.status = ok;
            }
            else if(ansLeft.status==brb){
                if (oldTree.color==BLACK){
                    ans.status=ok;
                }
                else {
                    ans.status=rrb;
                }
                ans.newTree = oldTree;
            }
            else if (colorOf(oldTree.right)==RED){
                colorFlip(oldTree);
                ans.newTree = oldTree;
                ans.status = brb;
            }
            else {
                ans.newTree = rebalLeft(oldTree, ansLeft.status);
                ans.status = rbr;
            }
        }
        return ans;
    }

    /**
     * rebal left
     * */
    static RBTree rebalLeft(RBTree oldTree, int leftStatus){
        RBTree l,m,r,lr,rl;
        if (leftStatus == rrb){
            r = oldTree;
            m = oldTree.left;
            l = m.left;
            rl = m.right;
            r.left = rl;
            m.right = r;
        }
        else {
            r = oldTree;
            l = oldTree.left;
            m = l.right;
            lr = m.left;
            rl = m.right;
            r.left = rl;
            l.right = lr;
            m.right = r;
            m.left = l;
        }
        l.color = RED;
        r.color = RED;
        m.color = BLACK;
        return m;
    }

    static InsReturn repairRight(RBTree oldTree, InsReturn ansRight){
        InsReturn ans = new InsReturn();
        if (ansRight.status == ok){
            ans.newTree = oldTree;
            ans.status = ok;
        }
        else{
            oldTree.right = ansRight.newTree;
            if (ansRight.status == rbr){
                ans.newTree = oldTree;
                ans.status = ok;
            }
            else if(ansRight.status==brb){
                if (oldTree.color==BLACK){
                    ans.status=ok;
                }
                else {
                    ans.status=brr;
                }
                ans.newTree = oldTree;
            }
            else if (colorOf(oldTree.left)==RED){
                colorFlip(oldTree);
                ans.newTree = oldTree;
                ans.status = brb;
            }
            else {
                ans.newTree = rebalRight(oldTree, ansRight.status);
                ans.status = rbr;
            }
        }
        return ans;
    }

    /**
     * rebal left
     * */
    static RBTree rebalRight(RBTree oldTree, int rightStatus){
        RBTree l,m,r,lr,rl;
        if (rightStatus == rrb){
            l = oldTree;
            r = oldTree.right;
            m = r.left;
            lr = m.left;
            rl = m.right;

            l.right = lr;
            m.left = l;
            m.right = r;
            r.left = rl;
        }
        else { //brr
            l = oldTree;
            m = oldTree.right;
            r = m.right;
            lr = m.left;
            l.right = lr;
            m.left = l;
        }
        l.color = RED;
        r.color = RED;
        m.color = BLACK;
        return m;
    }

    /**
     * 翻转颜色
     * 当前节点　和　左子节点　右子节点的颜色翻转
     * */
    static void colorFlip(RBTree rbt){
        rbt.color = reverseColor(rbt.color);
        if (rbt.left!=null){rbt.left.color = reverseColor(rbt.left.color);}
        if (rbt.right!=null){rbt.right.color = reverseColor(rbt.right.color);}
    }

    static int colorOf(RBTree rbt){
        if (rbt == null ){return BLACK;}
        return rbt.color;
    }

    /**
     * 得到翻转颜色
     * */
    static int reverseColor(int color){
        if (color==RED){return BLACK;}
        return RED;
    }
    /**********************   insert   end  *********************************/

    /**********************   delete begin  *********************************/
    /**
     * @param val 删除的节点
     * */
    static RBTree rbtDelete(RBTree rbt,int val){
        RBTree cur = rbt;
        RBTree parent = null;
        boolean needAdj = false;
        int subs = val;

        //找到节点位置，若能直接删除则删除,不能的话留待调整时递归删除
        while(true){
            if (cur == null){
                System.out.println("don't contain " + val);
                return rbt;
            }
            if (val == cur.key){
                //只有一个根节点
                if (parent==null && isLeaf(cur)){
                    return null;
                }
                //红叶节点: 删掉就行
                else if (isLeaf(cur) && cur.color==RED){
                    if (parent.left==cur){parent.left = null;}
                    else {parent.right = null;}
                    return rbt;
                }
                //无左子树,有右红子树: 红节点补上就行
                else if (cur.left == null && cur.right!=null){
                    cur.key = cur.right.key;
                    cur.right = null;
                    return rbt;
                }
                else if (isLeaf(cur) && cur.color==BLACK){
                    needAdj = true;
                    break;
                }
                //有左子树
                else if (cur.left != null){
                    RBTree deleteNode = findLeftMax(cur);
                    //左子树的max有左红节点：　左红节点补上即可
                    if (deleteNode.left != null && deleteNode.left.color==RED){
                        subs = deleteNode.key;
                        deleteNode.key = deleteNode.left.key;
                        deleteNode.left = null;
                        break;
                    }
                    //左max红节点：　后面调整时直接删掉
                    else if (isLeaf(deleteNode) && deleteNode.color == RED){
                        subs = deleteNode.key;
                        needAdj = true;
                        break;
                    }
                    else if (isLeaf(deleteNode) && deleteNode.color == BLACK){
                        subs = deleteNode.key;
                        needAdj = true;
                        break;
                    }
                }
            }
            if (val < cur.key){
                parent = cur;
                cur = cur.left;
            }
            if (val > cur.key){
                parent = cur;
                cur = cur.right;
            }
        }

        //不需要调整
        if (needAdj){
//            cur.key = subs;
            rbt = delNode(rbt, subs).newTree;
        }
        cur.key = subs;
        return rbt;
    }


    static final int LOW = 1;
    static final int OK = 0;
    /**
     *
     * */
    static class DelReturn{
        public RBTree newTree;
        public int status;
    }

    /**
     *
     * */
    static DelReturn delNode(RBTree oldTree, int toDelete){
        DelReturn ans, ansLeft, ansRight;
        if (toDelete<oldTree.key){
            ansLeft = delNode(oldTree.left, toDelete);
            ans = adjLeft(oldTree, ansLeft);
        }
        else if (toDelete>oldTree.key){
            ansRight = delNode(oldTree.right, toDelete);
            ans = adjRight(oldTree, ansRight);
        }
        //相等时要删掉
        else {
            ans = new DelReturn();
            ans.newTree = null;
            if (oldTree.color == BLACK){
                ans.status = LOW;
            }
            //红节点，调整时直接删除
            else {
                ans.status = OK;
            }
        }
        return ans;
    }

    static DelReturn adjLeft(RBTree oldTree, DelReturn ansLeft){
        DelReturn ret;
        oldTree.left = ansLeft.newTree;
        // ok的子树高度,无需处理直接返回
        if (ansLeft.status==OK){
            ret = new DelReturn();
            ret.newTree = oldTree;
            ret.status = OK;
            return ret;
        }
        //左子树比较低
        if (ansLeft.status == LOW){
            return delRebalLeft(oldTree);
        }

        return null;
    }

    static DelReturn delRebalLeft(RBTree rbt){
        DelReturn ret = new DelReturn();

        RBTree g,p,s,l,r,ll,lr;
        g = rbt.left;
        p = rbt;
        s = p.right;
        l = s!=null?s.left:null;
        r = s!=null?s.right:null;

        if (colorOf(l)==RED){
            ll = l.left;
            lr = l.right;

            l.color = p.color;
            p.color = BLACK;
            l.left = p;
            p.left = g;
            p.right = ll;
            s.left =lr;

            l.right = s;

            ret.newTree = l;
            ret.status =OK;
        }
        else if (colorOf(p)==RED){
            s.left = p;
            p.left = g;
            p.right = l;

            ret.newTree = s;
            ret.status =OK;
        }
        else if (colorOf(r)==RED){
            r.color = BLACK;

            s.left = p;
            p.right = l;

            s.right = r;

            ret.newTree = s;
            ret.status =OK;
        }
        else if (colorOf(s)==RED){
            s.color = BLACK;
            p.color = RED;

            s.left = p;
            p.right = l;

            s.left = delRebalLeft(p).newTree;
            ret.newTree = s;
            ret.status =OK;
        }
        else{
            s.color = RED;

            ret.newTree = p;
            ret.status =LOW;
        }
        return ret;
    }

    static DelReturn adjRight(RBTree oldTree, DelReturn ansRight){
        DelReturn ret;
        oldTree.right = ansRight.newTree;
        // ok的子树高度,无需处理直接返回
        if (ansRight.status==OK){
            ret = new DelReturn();
            ret.newTree = oldTree;
            ret.status = OK;
            return ret;
        }
        //左子树比较低
        if (ansRight.status == LOW){
            return delRebalRight(oldTree);
        }

        return null;
    }

    static DelReturn delRebalRight(RBTree rbt){
        DelReturn ret = new DelReturn();

        RBTree g,p,s,l,r,ll,lr,rl,rr;
        g = rbt.right;
        p = rbt;
        s = p.left;
        l = s!=null?s.left:null;
        r = s!=null?s.right:null;

        if (colorOf(r)==RED){
            rl = r.left;
            rr = r.right;

            r.color = p.color;
            p.color = BLACK;

            r.left = s;
            s.right = rl;

            r.right = p;
            p.left = rr;

            ret.newTree = r;
            ret.status =OK;
        }
        else if (colorOf(p)==RED){
            s.right = p;
            p.left = r;

            ret.newTree = s;
            ret.status =OK;
        }
        else if (colorOf(l)==RED){
            l.color = BLACK;

            s.left = l;

            s.right = p;
            p.left = r;

            ret.newTree = s;
            ret.status =OK;
        }
        else if (colorOf(s)==RED){
            s.color =BLACK;
            p.color = RED;

            s.right = p;
            p.left = r;

            s.right = delRebalRight(p).newTree;
            ret.newTree = s;
            ret.status =OK;
        }
        else{
            s.color = RED;

            ret.newTree = p;
            ret.status =LOW;
        }
        return ret;
    }

    /**
     * 是不是叶节点
     * */
    static boolean isLeaf(RBTree rbTree){
        return rbTree.left==null && rbTree.right==null;
    }

    /**
     * 找到左子树最大值
     * */
    static RBTree findLeftMax(RBTree rbt){
        RBTree cur = rbt.left;
        while (cur.right != null){
            cur = cur.right;
        }
        return cur;
    }

    /**********************   delete end  *********************************/

    /****************************************************************************************************/
    //　上　　　　　我的代码区域
    /****************************************************************************************************/

    /*
     * 获取Black Height，如果不满足定义（即两棵子树的高度不同），则返回-1；
     * 即这个函数同时也可以检查高度约束。
     */
    int getBlackHeight()
    {
        int lHeight;
        if(this.left == null)
            lHeight = 1;
        else
        {
            lHeight = this.left.getBlackHeight();
            if(lHeight < 0)
                return -1;
        }

        int rHeight;
        if(this.right == null)
            rHeight = 1;
        else
        {
            rHeight = this.right.getBlackHeight();
            if(rHeight < 0)
                return -1;
        }

        if(lHeight == rHeight)
            return lHeight + (this.color == BLACK ? 1 : 0);

        return -1;
    }

    private static boolean checkOrder(RBTree rbt)
    {
        curValue = -1;
        return checkOrder_Rec(rbt);
    }
    /*
     * 递归地检查大小约束，以中根序遍历红黑树，检查各个结点的key是否递增的。
     */
    private static int curValue;
    private static boolean checkOrder_Rec(RBTree rbt)
    {
        if(rbt == null)
            return true;

        if(!checkOrder_Rec(rbt.left))
            return false;

        if(rbt.key < curValue)
            return false;
        curValue = rbt.key;

        if(!checkOrder_Rec(rbt.right))
            return false;

        return true;
    }

    private static boolean outputElements_Rec(RBTree rbt)
    {
        if(rbt == null)
            return true;

        if(!outputElements_Rec(rbt.left))
            return false;

        System.out.print("\t" + rbt.key);

        if(!outputElements_Rec(rbt.right))
            return false;

        return true;
    }

    /*
     * 检查颜色约束, 但是根节点可以是红色（也就是说，可能是ARB树）
     */
    boolean checkColor()
    {
        int leftColor;
        if(this.left == null)
            leftColor = BLACK;
        else
        {
            if(!this.left.checkColor())
                return false;
            leftColor = this.left.color;
        }
        int rightColor;
        if(this.right == null)
            rightColor = BLACK;
        else
        {
            if(!this.right.checkColor())
                return false;
            rightColor = this.right.color;
        }

        if(this.color == BLACK)
            return true;
        if(leftColor == RED || rightColor == RED)
            return false;
        return true;
    }


    public String toString()
    {
        String s = "(";
        if(this.left == null)
            s += "NIL";
        else
            s += this.left.toString();
        s += ", (";
        s += "" + this.key + "," + (this.color == RED ? "red" : "black") + "), ";

        if(this.right == null)
            s += "NIL";
        else
            s += this.right.toString();

        s += ")";

        return s;
    }


    static String errorMsg;
    static int nextChar;
    static int nextPos;
    static char[] inputBuf;
    static String scannedInput;


    /*
     * 扫描分析s，获得一棵红黑树
     */
    static RBTree getRBTFromString(String s)
    {
        inputBuf = s.toCharArray();
        errorMsg = null;
        nextChar = inputBuf[0];
        nextPos = 0;

        RBTree rbt =  getRBTFromInput_Rec();
        if(errorMsg != null)
        {
            System.out.println("\t" + s.substring(0,nextPos));
            System.out.println("\t" + errorMsg);
            return null;
        }
        return rbt;
    }

    /****************************************************************************************************/
    //后面的代码都是用于扫描的子程序，不需要细看，有问题在QQ反映
    /****************************************************************************************************/
    private static RBTree getRBTFromInput_Rec()
    {
        if(tryToGetNIL())
            return null;

        getLeftPara();
        if(errorMsg != null)
            return null;

        RBTree left = getRBTFromInput_Rec();
        if(errorMsg != null)
            return null;

        getCOMMA();
        if(errorMsg != null)
            return null;

        getLeftPara();
        if(errorMsg != null)
            return null;

        int key = getInteger();
        if(errorMsg != null)
            return null;

        getCOMMA();
        if(errorMsg != null)
            return null;

        int color = getCOLOR();
        if(errorMsg != null)
            return null;

        getRightPara();
        if(errorMsg != null)
            return null;

        getCOMMA();
        if(errorMsg != null)
            return null;

        RBTree right = getRBTFromInput_Rec();
        if(errorMsg != null)
            return null;

        getRightPara();
        if(errorMsg != null)
            return null;

        return new RBTree(left, key, color, right);
    }

    private static void getCOMMA() {
        skipBlank();
        if(nextChar == ',')
        {
            nextChar = getAChar();
            skipBlank();
        }
        else
        {
            errorMsg = "\',\' expected!";
        }
    }

    private static void getLeftPara() {
        skipBlank();
        if(nextChar == '(')
        {
            nextChar = getAChar();
            skipBlank();
        }
        else
        {
            errorMsg = "\'(\' expected!";
        }
    }

    private static void getRightPara() {
        skipBlank();
        if(nextChar == ')')
        {
            nextChar = getAChar();
            skipBlank();
        }
        else
        {
            errorMsg = "\')\' expected!";
        }
    }

    static private boolean tryToGetNIL() {
        int prevPos = nextPos;
        skipBlank();
        String id = getID();
        if ("NIL".equals(id))
            return true;

        nextPos = prevPos;
        nextChar = nextPos < inputBuf.length ? inputBuf[nextPos] : -1;

//		errorMsg = "\"NIL\" expected.";
        return false;
    }

    private static int getCOLOR() {
        skipBlank();
        String id = getID();
        if("black".equals(id))
            return BLACK;
        else if("red".equals(id))
            return RED;
        else
            errorMsg = "Black/Red expected.";
        return -1;
    }



    private static String getID() {
        String ret = "";
        while((nextChar >= 'a' && nextChar <= 'z')|| (nextChar >= 'A' && nextChar <= 'Z'))
        {
            ret += (char)nextChar;
            nextChar = getAChar();
        }
        return ret;
    }

    private static int getInteger() {
        int ret = 0;
        while(nextChar >= '0' && nextChar <= '9')
        {
            ret = ret * 10 + (nextChar - '0');
            nextChar = getAChar();
        }
        if((nextChar >= 'a' && nextChar <= 'z')|| (nextChar >= 'A' && nextChar == 'Z'))
        {
            errorMsg = "A char following an integer.";
        }
        return ret;
    }


    private static void skipBlank() {
        while(nextChar == ' ' || nextChar == '\n' || nextChar == '\t')
            nextChar = getAChar();
    }

    private static int getAChar()
    {
        nextPos ++;
        if(nextPos >= inputBuf.length)
            nextChar = -1;
        else
            nextChar = inputBuf[nextPos];
        return nextChar;
    }

    private static String getALineFromInput() {
        String ret = "";
        for(;;)
        {
            int nChar;
            try {
                nChar = System.in.read();
            } catch (IOException e) {
                return null;
            }
            if(nChar == '\n')
                break;
            ret += (char)nChar;
        }
        return ret;
    }


}