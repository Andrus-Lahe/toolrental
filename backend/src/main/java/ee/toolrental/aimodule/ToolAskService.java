package ee.toolrental.aimodule;

import org.springframework.stereotype.Service;

@Service
public class ToolAskService {

    private final ToolSearchSqlGenerator sqlGenerator;
    private final ToolSearchSqlValidator sqlValidator;
    private final ToolSearchQueryRepository queryRepository;
    private final ToolSearchAnswerGenerator answerGenerator;

    public ToolAskService(ToolSearchSqlGenerator sqlGenerator,
                          ToolSearchSqlValidator sqlValidator,
                          ToolSearchQueryRepository queryRepository,
                          ToolSearchAnswerGenerator answerGenerator) {
        this.sqlGenerator = sqlGenerator;
        this.sqlValidator = sqlValidator;
        this.queryRepository = queryRepository;
        this.answerGenerator = answerGenerator;
    }

    public ToolAskResponse ask(String question) {
        String generatedSql = sqlGenerator.generateSql(question);
        if (ToolSearchSqlGenerator.CANNOT_ANSWER.equals(generatedSql)) {
            throw new UnsupportedToolSearchQuestionException();
        }

        ToolSearchSqlValidator.ValidatedSql validatedSql;
        try {
            validatedSql = sqlValidator.validate(generatedSql);
        } catch (InvalidToolSearchSqlException firstValidationFailure) {
            generatedSql = sqlGenerator.repairSql(question, firstValidationFailure);
            if (ToolSearchSqlGenerator.CANNOT_ANSWER.equals(generatedSql)) {
                throw new UnsupportedToolSearchQuestionException();
            }
            validatedSql = sqlValidator.validate(generatedSql);
        }
        ToolSearchSummary summary = ToolSearchSummary.from(validatedSql,
                queryRepository.searchWithCounts(validatedSql));
        String answer = answerGenerator.generateAnswer(summary.answerContext(question));
        return new ToolAskResponse(answer, summary.tools());
    }
}
