
#include <iostream>
#include <fstream>
#include <vector>
#include <string>
#include <cctype>
#include <unordered_map>
#include <map>

using namespace std;

// ����ؼ��ֵ���ǵ�ӳ�䣬���ڴʷ�����
unordered_map<string, string> keywordTokens = {
    {"main", "MAINTK"}, {"while", "WHILETK"}, {"const", "CONSTTK"}, {"int", "INTTK"},
    {"break", "BREAKTK"}, {"continue", "CONTINUETK"}, {"if", "IFTK"}, {"else", "ELSETK"},
    {"void", "VOIDTK"}, {"return", "RETURNTK"}, {"printf", "PRINTFTK"}, {"getint", "GETINTTK"},
    {"&&", "AND"}, {"||", "OR"}, {"==", "EQL"}, {"!=", "NEQ"}, {"<=", "LEQ"}, {">=", "GEQ"},
    {"<", "LSS"}, {">", "GRE"}, {"=", "ASSIGN"}, {"+", "PLUS"}, {"-", "MINU"},
    {"*", "MULT"}, {"/", "DIV"}, {"%", "MOD"}, {"!", "NOT"}, {";", "SEMICN"},
    {",", "COMMA"}, {"(", "LPARENT"}, {")", "RPARENT"}, {"[", "LBRACK"}, {"]", "RBRACK"},
    {"{", "LBRACE"}, {"}", "RBRACE"}
};

// �洢���н������ tokens
vector<pair<string, string>> tokenList;

// ����Ƿ��ǹؼ���
bool checkKeyword(const string& str) {
    return keywordTokens.find(str) != keywordTokens.end();
}

// �ж��Ƿ��Ǳ�ʶ���ַ�
bool isIdentifierStart(char c) {
    return isalpha(c) || c == '_';
}

// �ж��Ƿ��������ַ�
bool isDigitCharacter(char c) {
    return isdigit(c);
}

// ���ಢ�洢 token
void classifyToken(const string& token) {
    if (checkKeyword(token)) {
        tokenList.emplace_back(keywordTokens[token], token);
    }
    else if (isIdentifierStart(token[0])) {
        tokenList.emplace_back("IDENFR", token);
    }
    else if (isDigitCharacter(token[0])) {
        tokenList.emplace_back("INTCON", token);
    }
}

// ����ע�͵��߼�����ȡ���������ĺ���
bool handleComments(char ch, const string& code, size_t& i, bool& in_single_comment, bool& in_multi_comment) {
    if (ch == '/') {
        if (i + 1 < code.size() && code[i + 1] == '/') {
            in_single_comment = true;
            i++; 
            return true;
        }
        if (i + 1 < code.size() && code[i + 1] == '*') {
            in_multi_comment = true;
            i++; 
            return true;
        }
    }
    return false;
}

// ���ַ�����ǵĽ���������Ϊ����
void parseStringToken(char ch, string& current_token, bool& in_string, vector<pair<string, string>>& tokenList) {
    current_token += ch;
    if (ch == '"' && (current_token.size() == 1 || current_token[current_token.size() - 2] != '\\')) {
        tokenList.emplace_back("STRCON", current_token);
        in_string = false;
        current_token.clear();
    }
}

// �ʷ���������
void processTokens(const string& code) {
    bool in_string = false;
    bool in_single_comment = false;
    bool in_multi_comment = false;
    string current_token;

    for (size_t i = 0; i < code.size(); ++i) {
        char ch = code[i];

        if (in_single_comment) {
            if (ch == '\n') in_single_comment = false;
            continue;
        }

        if (in_multi_comment) {
            if (ch == '*' && i + 1 < code.size() && code[i + 1] == '/') {
                in_multi_comment = false;
                i++;
            }
            continue;
        }

        if (in_string) {
            parseStringToken(ch, current_token, in_string, tokenList);
            continue;
        }

        if (ch == '"') {
            in_string = true;
            current_token += ch;
            continue;
        }

        if (handleComments(ch, code, i, in_single_comment, in_multi_comment)) continue;

        if (isspace(ch)) {
            if (!current_token.empty()) {
                classifyToken(current_token);
                current_token.clear();
            }
            continue;
        }

        if (isalnum(ch) || ch == '_') {
            current_token += ch;
        }
        else {
            if (!current_token.empty()) {
                classifyToken(current_token);
                current_token.clear();
            }

            string next_two_chars = code.substr(i, 2);
            if (next_two_chars == "!=" || next_two_chars == "==" || next_two_chars == "&&" || next_two_chars == "||" ||
                next_two_chars == "<=" || next_two_chars == ">=") {
                classifyToken(next_two_chars);
                i++;
            }
            else {
                classifyToken(string(1, ch));
            }
        }
    }

    if (!current_token.empty()) {
        classifyToken(current_token);
    }
}

// ���ļ��ж�ȡ�����дʷ�����
void lexicalAnalysis(const string& inputFile) {
    ifstream infile(inputFile);

    if (!infile.is_open()) {
        cerr << "Error opening file!" << endl;
        return;
    }

    string code((istreambuf_iterator<char>(infile)), istreambuf_iterator<char>());
    processTokens(code);
    infile.close();
}

// ƥ�䲢����ؼ���
void matchToken(string expected) {
    if (!tokenList.empty() && tokenList[0].first == expected) {
        tokenList.erase(tokenList.begin());
    } else {
        cerr << "Unexpected token!" << endl;
    }
}

// ���뵥Ԫ������
void parseCompUnit() {
    while (!tokenList.empty()) {
        if (tokenList[0].first == "INTTK" || tokenList[0].first == "VOIDTK") {
            matchToken("INTTK");
        } else {
            break;
        }
    }
}

// �﷨��������ڵ�
void syntaxAnalysis() {
    parseCompUnit();
}

// ������
int main() {
    lexicalAnalysis("testfile.txt");
    syntaxAnalysis();
    return 0;
}

