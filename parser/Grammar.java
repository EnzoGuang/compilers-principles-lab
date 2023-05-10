package parser;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;

public class Grammar {
    private LinkedHashSet<String> vn = new LinkedHashSet<>();
    private LinkedHashSet<String> vt = new LinkedHashSet<>();
    private LinkedHashMap<String, ArrayList<String>> grammar = new LinkedHashMap<>();

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
    public void elminateIndirectLeftRecursive() {
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

    public static void main(String[] args) {
        String[] input1 = {"E->E+T|T", "T->T*F|F", "F->(E)|i"};
        String[] input2 = {"M->MaH|H", "H->b(M)|(M)|b"};
        String[] input3 = {"S->(XE)|F)", "X->E)|F]", "E->A", "F->A", "A->ε"};
        String[] input4 = {"C->Ac|c", "B->Cb|b", "A->Ba|a"};
        String[] input5 = {"R->Sa|a", "Q->Rb|b", "S->Qc|c"};
        Grammar grammar = new Grammar(input4);
        grammar.printGrammar();
        System.out.println("VN: " + grammar.getVN());
        System.out.println("VT: " + grammar.getVT());
        System.out.println("candidate: " + grammar.getCandidate("B"));
        System.out.println("orderOfVn: " + grammar.confirmVnOrder());
        grammar.elminateIndirectLeftRecursive();
        grammar.printGrammar();
    }
}