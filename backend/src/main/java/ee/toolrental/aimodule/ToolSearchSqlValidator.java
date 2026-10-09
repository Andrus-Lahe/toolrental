package ee.toolrental.aimodule;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class ToolSearchSqlValidator {

    private static final int MAX_SQL_LENGTH = 2_000;
    private static final List<String> REQUIRED_COLUMNS = List.of(
            "tool_id", "tool_name", "category_name", "location_name");

    public ValidatedSql validate(String sql) {
        if (sql == null || sql.isBlank() || sql.length() > MAX_SQL_LENGTH) {
            throw invalidQuery();
        }

        Parser parser = new Parser(tokenize(sql));
        parser.expectWord("SELECT");
        parser.expectProjection();
        parser.expectWord("FROM");
        parser.expectWord("tool_rental");
        parser.expectSymbol(".");
        parser.expectWord("ai_tool_search");
        parser.expectWord("WHERE");
        parser.expectAvailabilityConditions();
        parser.expectWord("ORDER");
        parser.expectWord("BY");
        parser.expectWord("tool_id");
        parser.consumeOptionalWord("ASC");
        parser.expectWord("LIMIT");
        int limit = parser.expectInteger();
        if (limit < 1 || limit > 20) {
            throw invalidQuery();
        }
        parser.consumeOptionalSymbol(";");
        parser.expectEnd();

        return new ValidatedSql(sql.trim());
    }

    private static List<Token> tokenize(String sql) {
        List<Token> tokens = new ArrayList<>();
        for (int index = 0; index < sql.length();) {
            char current = sql.charAt(index);
            if (Character.isWhitespace(current)) {
                index++;
            } else if (current == '\'') {
                int start = index++;
                boolean closed = false;
                while (index < sql.length()) {
                    if (sql.charAt(index++) == '\'') {
                        if (index < sql.length() && sql.charAt(index) == '\'') {
                            index++;
                        } else {
                            closed = true;
                            break;
                        }
                    }
                }
                if (!closed) {
                    throw invalidQuery();
                }
                tokens.add(new Token(TokenType.STRING, sql.substring(start, index)));
            } else if (Character.isLetter(current) || current == '_') {
                int start = index++;
                while (index < sql.length()) {
                    char next = sql.charAt(index);
                    if (!Character.isLetterOrDigit(next) && next != '_') {
                        break;
                    }
                    index++;
                }
                tokens.add(new Token(TokenType.WORD, sql.substring(start, index)));
            } else if (Character.isDigit(current)) {
                int start = index++;
                while (index < sql.length() && Character.isDigit(sql.charAt(index))) {
                    index++;
                }
                tokens.add(new Token(TokenType.NUMBER, sql.substring(start, index)));
            } else if (",.;=<>".indexOf(current) >= 0) {
                tokens.add(new Token(TokenType.SYMBOL, Character.toString(current)));
                index++;
            } else {
                // Reject comments, quoted identifiers, operators, parentheses and SQL syntax
                // outside the deliberately small search-query grammar.
                throw invalidQuery();
            }
        }
        return tokens;
    }

    private static InvalidToolSearchSqlException invalidQuery() {
        return new InvalidToolSearchSqlException();
    }

    public static final class ValidatedSql {
        private final String sql;

        private ValidatedSql(String sql) {
            this.sql = sql;
        }

        String value() {
            return sql;
        }
    }

    private static final class Parser {
        private final List<Token> tokens;
        private int position;
        private boolean hasAvailabilityFilter;

        private Parser(List<Token> tokens) {
            this.tokens = tokens;
        }

        private void expectProjection() {
            for (int index = 0; index < REQUIRED_COLUMNS.size(); index++) {
                expectWord(REQUIRED_COLUMNS.get(index));
                if (index < REQUIRED_COLUMNS.size() - 1) {
                    expectSymbol(",");
                }
            }
        }

        private void expectAvailabilityConditions() {
            do {
                String column = expectAnyWord("tool_id", "tool_name", "category_name",
                        "location_name", "is_available_now");
                String operator = next().value().toUpperCase(Locale.ROOT);
                if (column.equals("is_available_now")) {
                    if (!operator.equals("=")) {
                        throw invalidQuery();
                    }
                    expectWord("TRUE");
                    hasAvailabilityFilter = true;
                } else if (column.equals("tool_id")) {
                    if (!operator.equals("=")) {
                        throw invalidQuery();
                    }
                    expectType(TokenType.NUMBER);
                } else {
                    if (!operator.equals("=") && !operator.equals("LIKE") && !operator.equals("ILIKE")) {
                        throw invalidQuery();
                    }
                    expectType(TokenType.STRING);
                }
            } while (consumeOptionalWord("AND"));

            if (!hasAvailabilityFilter) {
                throw invalidQuery();
            }
        }

        private String expectAnyWord(String... allowed) {
            Token token = expectType(TokenType.WORD);
            String value = token.value().toLowerCase(Locale.ROOT);
            for (String candidate : allowed) {
                if (candidate.equals(value)) {
                    return value;
                }
            }
            throw invalidQuery();
        }

        private void expectWord(String expected) {
            Token token = expectType(TokenType.WORD);
            if (!token.value().equalsIgnoreCase(expected)) {
                throw invalidQuery();
            }
        }

        private void expectSymbol(String expected) {
            Token token = expectType(TokenType.SYMBOL);
            if (!token.value().equals(expected)) {
                throw invalidQuery();
            }
        }

        private int expectInteger() {
            try {
                return Integer.parseInt(expectType(TokenType.NUMBER).value());
            } catch (NumberFormatException exception) {
                throw invalidQuery();
            }
        }

        private Token expectType(TokenType type) {
            Token token = next();
            if (token.type() != type) {
                throw invalidQuery();
            }
            return token;
        }

        private Token next() {
            if (position >= tokens.size()) {
                throw invalidQuery();
            }
            return tokens.get(position++);
        }

        private boolean consumeOptionalWord(String expected) {
            if (position < tokens.size() && tokens.get(position).type() == TokenType.WORD
                    && tokens.get(position).value().equalsIgnoreCase(expected)) {
                position++;
                return true;
            }
            return false;
        }

        private boolean consumeOptionalSymbol(String expected) {
            if (position < tokens.size() && tokens.get(position).type() == TokenType.SYMBOL
                    && tokens.get(position).value().equals(expected)) {
                position++;
                return true;
            }
            return false;
        }

        private void expectEnd() {
            if (position != tokens.size()) {
                throw invalidQuery();
            }
        }
    }

    private enum TokenType { WORD, NUMBER, STRING, SYMBOL }

    private record Token(TokenType type, String value) {}
}
