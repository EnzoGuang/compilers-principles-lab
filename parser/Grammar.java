package parser;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;

public class Grammar {
    private LinkedHashSet<String> vn = new LinkedHashSet<>();
    private LinkedHashSet<String> vt = new LinkedHashSet<>();
    private LinkedHashMap<String, ArrayList<String>> grammar = new LinkedHashMap<>();
    private LinkedHashMap<String, ArrayList<Character>> vnFirst = new LinkedHashMap<>();

    /* 接收一个字符串数组，获得文法. */
    public Grammar(String[] grammarContent) {
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
            System.out.println("不是非终结符");
            return null;
        } else {
            return grammar.get(vn);
        }
    }

    /* 识别并添加终结符 */
    public void addVT() {
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
    public int getIndexOfChar(char x, ArrayList<String> vn) {
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
    public void getVnFirst() {
        ArrayList<String> vnOrder = confirmVnOrder();
        /* 初始化各个非终结符的first集(全为空) */
        for (String temp: vnOrder) {
            ArrayList<Character> first = new ArrayList<>();
            vnFirst.put(temp, first);
        }
        for (String temp: vnOrder) {
            getVnFirst(temp);
        }
    }

    /* 计算非终结符vn的first集合，当中有递归调用 */
    private ArrayList<Character> getVnFirst(String vn) {
        ArrayList<String> candidate = getCandidate(vn);
        ArrayList<Character> vnFirstSet = vnFirst.get(vn);
        ArrayList<Character> update;
        for (String temp: candidate) {
            char firstChar = temp.charAt(0);
            if (!isVN(String.valueOf(firstChar))) {
                if (!vnFirstSet.contains(firstChar)) {
                    vnFirstSet.add(firstChar);
                }
            } else {
                update = getVnFirst(String.valueOf(firstChar));
                for (Character first: update) {
                    if (!vnFirstSet.contains(first)) {
                        vnFirstSet.add(first);
                    }
                }
            }
        }
        return vnFirstSet;
    }
}
