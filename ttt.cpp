
#include <iostream>
#include <fstream>
#include <vector>
#include <string>
#include <cctype>
#include <unordered_map>
#include <map>

using namespace std;

// 定义关键字到标记的映射，用于词法分析
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

// 存储所有解析后的 tokens
vector<pair<string, string>> tokenList;

// 检查是否是关键字
bool checkKeyword(const string& str) {
    return keywordTokens.find(str) != keywordTokens.end();
}

// 判断是否是标识符字符
bool isIdentifierStart(char c) {
    return isalpha(c) || c == '_';
}

// 判断是否是数字字符
bool isDigitCharacter(char c) {
    return isdigit(c);
}

// 分类并存储 token
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

// 处理注释的逻辑，提取出来单独的函数
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

// 将字符串标记的解析单独作为函数
void parseStringToken(char ch, string& current_token, bool& in_string, vector<pair<string, string>>& tokenList) {
    current_token += ch;
    if (ch == '"' && (current_token.size() == 1 || current_token[current_token.size() - 2] != '\\')) {
        tokenList.emplace_back("STRCON", current_token);
        in_string = false;
        current_token.clear();
    }
}

// 词法分析函数
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

// 从文件中读取并进行词法分析
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

// 匹配并处理关键字
void matchToken(string expected) {
    if (!tokenList.empty() && tokenList[0].first == expected) {
        tokenList.erase(tokenList.begin());
    } else {
        cerr << "Unexpected token!" << endl;
    }
}

// 编译单元处理函数
void parseCompUnit() {
    while (!tokenList.empty()) {
        if (tokenList[0].first == "INTTK" || tokenList[0].first == "VOIDTK") {
            matchToken("INTTK");
        } else {
            break;
        }
    }
}

// 语法分析的入口点
void syntaxAnalysis() {
    parseCompUnit();
}

// 主函数
int main() {
    lexicalAnalysis("testfile.txt");
    syntaxAnalysis();
    return 0;
}

