/** 构造自上而下的非递归的语法分析器
 *   求解算法步骤:
 *  1.消除左递归(包括直接左递归和间接左递归)
 *  2.求所有非终结符的First集
 *  3.求所有非终结符的Follow集
 *  4.求所有候选式的Select集
 *  5.构建分析表
 *  6.构建分析栈
 *
 * @author 杨光
 * @date 2023/05/19
 */
package parser;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.ArrayDeque;

public class Parser {
    private LinkedHashSet<String> vn = new LinkedHashSet<>();
    private LinkedHashSet<String> vt = new LinkedHashSet<>();
    private LinkedHashMap<String, ArrayList<String>> grammar = new LinkedHashMap<>();
    private LinkedHashMap<String, ArrayList<Character>> vnFirst = new LinkedHashMap<>();
    private LinkedHashMap<String, ArrayList<String>> vnFollow = new LinkedHashMap<>();
    private LinkedHashMap<String, ArrayList<String>> vnSelect = new LinkedHashMap<>();
    private ArrayDeque<String> analyzeStack = new ArrayDeque<>();

    /* 接收一个字符串数组，获得文法. */
    public Parser(String[] grammarContent) {
        String[] candidate;
        for (String temp: grammarContent) {
            /* 通过正则表达式匹配"->"符号 */
            String[] content = temp.split("->");
            /* 匹配"|", 区分产生式的不同候选式 */
            candidate = content[1].split("\\|");
            ArrayList<String> candidateList = new ArrayList<>();
            for (String c: candidate) {
                candidateList.add(c);
            }
            grammar.put(content[0], candidateList);
            vn.add(content[0]);
        }
        addVT();
    }

    /* 打印文法 */
    public void printGrammar() {
        System.out.println("产生式如下: ");
        for (String key: grammar.keySet()) {
            for (String valueList: grammar.get(key)) {
                System.out.print(key + " -> ");
                System.out.print(valueList + " ");
                System.out.println();
            }
        }
        System.out.println();
    }

    /* 判断是否是非终结符 */
    public boolean isVN(String content) {
        return vn.contains(content);
    }

    /* 以列表形式返回非终结符 */
    public ArrayList<String> getVN() {
        ArrayList<String> result = new ArrayList<>();
        for (String temp: this.vn) {
            result.add(temp);
        }
        return result;
    }

    /* 以列表形式返回终结符 */
    public ArrayList<String> getVT() {
        ArrayList<String> result = new ArrayList<>();
        for (String temp: this.vt) {
            result.add(temp);
        }
        return result;
    }

    /* 得到参数vn的所有候选式，如果vn不是非终结符，程序报错 */
    public ArrayList<String> getCandidate(String vn) {
        if (!isVN(vn)) {
            System.out.println(vn + "不是非终结符");
            return null;
        } else {
            return grammar.get(vn);
        }
    }

    public ArrayList<Character> getFirstOfVn(String vn) {
        return vnFirst.get(vn);
    }

    public ArrayList<String> getFollowOfVn(String vn) {
        return vnFollow.get(vn);
    }

    /* 得到所有vn的产生式,以A->B的形式*/
    public ArrayList<String> getProduction(String vn) {
        ArrayList<String> result = new ArrayList<>();
        ArrayList<String> candidate = getCandidate(vn);
        for (String content: candidate) {
            result.add(vn + "->" + content);
        }
        return result;

    }

    /* 识别并添加终结符 */
    private void addVT() {
        for (String key: grammar.keySet()) {
            for (String value: grammar.get(key)) {
                for (int i = 0; i < value.length(); i++) {
                    if (!isVN(String.valueOf(value.charAt(i)))) {
                        vt.add(String.valueOf(value.charAt(i)));
                    }
                }
            }
        }
    }

    /* 确定非终结符的顺序，为消除左递归提供便利 */
    public ArrayList<String> confirmVnOrder() {
        return getVN();
    }

    /* 判断字符x是否在某个候选式中 */
    private int getIndexOfChar(char x, ArrayList<String> vn) {
        for (int i = 0; i < vn.size(); i++) {
            if (vn.get(i).contains(String.valueOf(x))) {
                return i;
            }
        }
        return -1;
    }

    /* 消除间接左递归 */
    public void eliminateIndirectLeftRecursive() {
        ArrayList<String> vnOrder = confirmVnOrder();
        for (int i = 0; i < vnOrder.size(); i++) {
            /* 获得当前非终结符的所有候选式 */
            ArrayList<String> candidate = getCandidate(vnOrder.get(i));
            int originalCandidateSize = candidate.size();
            for (int j = 0; j < originalCandidateSize; j++) {
                /* 获得当前候选式的首字符 */
                char firstChar = candidate.get(j).charAt(0);
                /* 判断当前字符在非终结符中的顺序 */
                int indexFirstChar = getIndexOfChar(firstChar, vnOrder);
                if (isVN(String.valueOf(firstChar)) && indexFirstChar >= 0 && indexFirstChar < i) {
                    ArrayList<String> modifyGrammar = getCandidate(String.valueOf(firstChar));
                    /* temp用来记录需要被修改的候选式 */
                    String temp = candidate.remove(j);
                    for (int k = 0; k < modifyGrammar.size(); k++) {
                        String result = temp.replaceAll(String.valueOf(firstChar), modifyGrammar.get(k));
                        candidate.add(result);
                    }
                }
            }
        }
    }

    /* 消除直接左递归 */
    public void eliminateDirectLeftRecursive() {
        /* 按照非终结符的顺序进行消除直接左递归 */
        ArrayList<String> vnOrder = confirmVnOrder();
        for (int i = 0; i < vnOrder.size(); i++) {
            ArrayList<String> candidate = getCandidate(vnOrder.get(i));
            int originalCandidateSize = candidate.size();
            ArrayList<String> recursivePrefix = new ArrayList<>(); //包含有直接左递归的候选式
            ArrayList<String> other = new ArrayList<>(); //包含没有直接左递归的候选式
            for (int j = 0; j < originalCandidateSize; j++) {
                String currentCandidate = candidate.get(j);
                char firstChar = currentCandidate.charAt(0);
                /* 判断当前候选式的第一个字符与当前产生式的左部的字符是否相等，判断是否直接左递归 */
                if (firstChar == vnOrder.get(i).charAt(0)) {
                    /** 如果是左递归把当前候选式加入包含左递归候选式的列表中
                     *  并把候选式的会产生左递归的非终结符先替换为空，为以后提供方便
                     */
                    recursivePrefix.add(currentCandidate.replaceAll(vnOrder.get(i), ""));
                } else {
                    other.add(currentCandidate);
                }
            }
            if (recursivePrefix.size() != 0) {
                ArrayList<String> newCandidate = new ArrayList<>();
                for (int j = 0; j < other.size(); j++) {
                    String temp = other.get(j);
                    newCandidate.add(temp + vnOrder.get(i) + '\'');
                }
                grammar.put(vnOrder.get(i), newCandidate);
                ArrayList<String> newVnCandidate = new ArrayList<>();
                for (int j = 0; j < recursivePrefix.size(); j++) {
                    newVnCandidate.add(recursivePrefix.get(j) + vnOrder.get(i) + '\'');
                }
                newVnCandidate.add("ε");
                grammar.put(vnOrder.get(i) + '\'', newVnCandidate);
                /* 把新产生的非终结符加入vn集合中 */
                vn.add(vnOrder.get(i) + '\'');
            }
        }
    }

    /* 计算所有非终结符的first集合 */
    public void calculateFirst() {
        ArrayList<String> vnOrder = confirmVnOrder();
        /* 初始化各个非终结符的first集(全为空) */
        for (String temp: vnOrder) {
            ArrayList<Character> first = new ArrayList<>();
            vnFirst.put(temp, first);
        }
        for (String temp: vnOrder) {
            calculateFirst(temp);
        }
    }

    /* 计算非终结符vn的first集合，当中有递归调用 */
    private ArrayList<Character> calculateFirst(String vn) {
        ArrayList<String> candidate = getCandidate(vn);
        ArrayList<Character> vnFirstSet = vnFirst.get(vn);
        ArrayList<Character> update;
        int sizeOfEmpty; //候选式中连续的非终结符的first集含有空的个数
        boolean isCurrentVnFirstEmpty; //判断当前候选式所处的非终结符的first集中是否含空
        for (String temp: candidate) {
            sizeOfEmpty = 0;
            isCurrentVnFirstEmpty = false;
            int index = 0;
            for (int i = 0; i < temp.length(); i++) {
                char firstChar = temp.charAt(index);
                /* 当前字符是终结符 */
                if (!isVN(String.valueOf(firstChar))) {
                    if (!vnFirstSet.contains(firstChar)) {
                        vnFirstSet.add(firstChar);
                        break;
                    }
                } else {
                    /* 当前字符是非终结符 */
                    update = calculateFirst(String.valueOf(firstChar));
                    for (Character content: update) {
                        if (!vnFirstSet.contains(content) && content != 'ε') {
                            vnFirstSet.add(content);
                        }
                        if (content == 'ε') {
                            index++;
                            isCurrentVnFirstEmpty = true;
                        }
                    }
                    if (isCurrentVnFirstEmpty) {
                        sizeOfEmpty++;
                    } else {
                        break;
                    }
                }
            }
            /** 如果非终结符含空的个数和当前候选式的长度相等的话，ε入first集合 */
            if (temp.length() == sizeOfEmpty) {
                if (!vnFirstSet.contains('ε')) {
                    vnFirstSet.add('ε');
                }
            }
        }
        return vnFirstSet;
    }

    /* 获得所有非终结符的Follow集 */
    public void calculateFollow() {
        ArrayList<String> vnOrder = confirmVnOrder();
        for (int i = 0; i < vnOrder.size(); i++) {
            ArrayList<String> temp = new ArrayList<>();
            if (i == 0) {
                temp.add(String.valueOf('#'));
            }
            vnFollow.put(vnOrder.get(i), temp);
        }
        for (String temp: vnOrder) {
            calculateFollow(temp);
        }
        for (String temp: vnOrder) {
            calculateFollow(temp);
        }
    }

    private void calculateFollow(String vn) {
        ArrayList<String> candidate = getCandidate(vn);
        for (int i = 0; i < candidate.size(); i++) {
            String currentCandidate = candidate.get(i);
            if (currentCandidate.equals('ε')) {
                continue;
            }
            for (int j = 0; j < currentCandidate.length();) {
                int emptySize = 0;
                String currentVn = currentCandidate.charAt(j) + "";
                currentVn = isQuote(j, currentCandidate);
                if (!isVN(currentVn)) {
                    j += currentVn.length();
                    continue;
                }
                ArrayList<String> currentFollow = getFollowOfVn(currentVn);
                for (int k = j + currentVn.length(); k < currentCandidate.length();) {
                    String nextVn = isQuote(k, currentCandidate);
                    if (isVN(nextVn)) {
                        boolean isEmpty = addVnFirstToFollow(nextVn, currentFollow);
                        if (isEmpty) {
                            emptySize += nextVn.length();
                        }
                    } else {
                        if (!currentFollow.contains(nextVn)) {
                            currentFollow.add(nextVn);
                        }
                        break;
                    }
                    k += nextVn.length();
                }
                if (j + currentVn.length() - 1 +  emptySize == currentCandidate.length() - 1) {
                    addLeftFollowToAnother(getFollowOfVn(vn), currentFollow);
                }
                j += currentVn.length();
            }
        }
    }

    /* 判断当前非终结符是否跟着',例如E' */
    private String isQuote(int currentIndex, String candidate) {
        String result = "" + candidate.charAt(currentIndex);
        if (currentIndex + 1 <= candidate.length() - 1) {
            if (candidate.charAt(currentIndex + 1) == '\'') {
                result += '\'';
            }
        }
        return result;
    }

    /* 将非终结符vn的First集合除去空后再全部加入Follow集合中，并返回First集合是否含空 */
    private boolean addVnFirstToFollow(String vn, ArrayList<String> follow) {
        ArrayList<Character> first = calculateFirst(vn);
        boolean isEmpty = false;
        for (Character temp: first) {
            if (!follow.contains(String.valueOf(temp))) {
                if (temp != 'ε') {
                    follow.add(String.valueOf(temp));
                } else {
                    isEmpty = true;
                }
            }
        }
        return isEmpty;
    }

    /* 将产生式左部非终结符的Follow集加入右部当前非终结符的Follow集 */
    private void addLeftFollowToAnother(ArrayList<String> from, ArrayList<String> to) {
        for (String temp: from) {
            if (!to.contains(temp)) {
                to.add(temp);
            }
        }
    }

    /* 获得所有候选式的Select集 */
    public void calculateSelect() {
        ArrayList<String> vnOrder = confirmVnOrder();
        for (String orderOfVn: vnOrder) {
            ArrayList<String> grammarOfvn = getCandidate(orderOfVn);
            for (String temp: grammarOfvn) {
                ArrayList<String> content = new ArrayList<>();
                char firstChar = temp.charAt(0);
                if (isVN(String.valueOf(firstChar)) && firstChar != 'ε') {
                    addFirstToSelect(content, String.valueOf(firstChar));
                } else if (firstChar == 'ε') {
                    addFollowToSelect(content, orderOfVn);
                } else {
                    addTerminateToSelect(content, String.valueOf(firstChar));
                }
                vnSelect.put(orderOfVn + "->" + temp, content);
            }
        }
    }

    /* 候选式右部第一个字符为非终结符，该非终结符的First集入Select集 */
    private void addFirstToSelect(ArrayList<String> content, String vn) {
        ArrayList<Character> temp = getFirstOfVn(vn);
        for (Character t: temp) {
            if (!content.contains(String.valueOf(t)) && t != 'ε') {
                content.add(String.valueOf(t));
            }
        }
    }

    /* 候选式右部为空，添加产生式左部的Follow集入Select集 */
    private void addFollowToSelect(ArrayList<String> content, String vn) {
        ArrayList<String> temp = getFollowOfVn(vn);
        for (String t: temp) {
            if (!content.contains(t)) {
                content.add(t);
            }
        }
    }

    /* 候选式右部第一个字符为终结符，终结符进Select集 */
    private void addTerminateToSelect(ArrayList<String> content, String vn) {
        if (!content.contains(vn)) {
            content.add(vn);
        }
    }

    /* 对输入串进行分析 */
    public void analyzeProcess(String sentence) {
        System.out.println("--------------------------------------------------");
        System.out.printf("%-8s%-12s%-10s%s\n", "步骤", "分析栈", "余留输入串", "所用产生式");
        sentence += "#";
        int stepIndex = 1;
        int indexSentence = 0;
        boolean flag = false;
        analyzeStack.addFirst("#");
        String startVn = confirmVnOrder().get(0);
        analyzeStack.addFirst(startVn);
        ArrayList<String> production = getProduction(startVn);
        while (true) {
            /* 如果栈顶为非终结符，和输入串当前字符比较 */
            if (!isVN(analyzeStack.peek())) {
                if (analyzeStack.peek().equals(String.valueOf(sentence.charAt(indexSentence)))) {
                    printStep(stepIndex, analyzeStack, sentence.substring(indexSentence), "");
                    if (analyzeStack.peek().equals("#") && sentence.substring(indexSentence).equals("#")) {
                        flag = true;
                        break;
                    } else {
                        analyzeStack.removeFirst();
                        stepIndex++;
                        if (sentence.charAt(indexSentence) != '#') {
                            indexSentence++;
                        }
                    }
                } else {
                    System.out.println("Analyze error!");
                    break;
                }
            } else if (isVN(analyzeStack.peek())) {
                production = getProduction(analyzeStack.peek());
                int limit = 0;
                for (String t: production) {
                    ArrayList<String> selectSet = vnSelect.get(t);
                    if (selectSet.contains(String.valueOf(sentence.charAt(indexSentence)))) {
                        printStep(stepIndex, analyzeStack, sentence.substring(indexSentence), t);
                        analyzeStack.removeFirst();
                        String[] leftAndRight = t.split("->");
                        reverseAdd(analyzeStack, leftAndRight[1]);
                        limit++;
                        stepIndex++;
                        break;
                    }
                }
                if (limit == 0) {
                    System.out.println("Analyze error! Select Set not contain!");
                    break;
                }
            }
        }
        if (flag) {
            System.out.println("Analyze success! Congratulation!");
        }
        System.out.println("--------------------------------------------------\n");
    }

    /* 以字符串形式获得分析栈的所有元素 */
    private String getElementOfStack(ArrayDeque<String> analyzeStack) {
        StringBuffer content = new StringBuffer();
        for (String t: analyzeStack) {
            content.insert(0, t);
        }
        return content.toString();
    }

    /* 打印每个步骤的信息，包括分析栈，使用的产生式 */
    private void printStep(int stepIndex, ArrayDeque<String> analyzeStack, String sentence, String production) {
        String content = getElementOfStack(analyzeStack);
        System.out.printf("%-10d%-15s%-15s%s\n", stepIndex, content, sentence, production);
    }

    /* 将产生式右部逆序加入分析栈中 */
    private void reverseAdd(ArrayDeque<String> analyzeStack, String right) {
        ArrayDeque<String> temp = new ArrayDeque<>();
        for (int i = 0; i < right.length();) {
            String currentVn = isQuote(i, right);
            temp.addFirst(currentVn);
            i += currentVn.length();
        }
        while (!temp.isEmpty()) {
            String content = temp.peekFirst();
            if (content.equals("ε")) {
                temp.removeFirst();
            } else {
                analyzeStack.addFirst(temp.removeFirst());
            }
        }
    }
}
