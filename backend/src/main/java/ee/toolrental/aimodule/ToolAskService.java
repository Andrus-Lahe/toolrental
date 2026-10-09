package ee.toolrental.aimodule;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ToolAskService {

    private final ToolSearchSqlGenerator sqlGenerator;
    private final ToolSearchSqlValidator sqlValidator;
    private final ToolSearchQueryRepository queryRepository;

    public ToolAskService(ToolSearchSqlGenerator sqlGenerator,
                          ToolSearchSqlValidator sqlValidator,
                          ToolSearchQueryRepository queryRepository) {
        this.sqlGenerator = sqlGenerator;
        this.sqlValidator = sqlValidator;
        this.queryRepository = queryRepository;
    }

    public ToolAskResponse ask(String question) {
        String generatedSql = sqlGenerator.generateSql(question);
        if (ToolSearchSqlGenerator.CANNOT_ANSWER.equals(generatedSql)) {
            throw new UnsupportedToolSearchQuestionException();
        }

        ToolSearchSqlValidator.ValidatedSql validatedSql = sqlValidator.validate(generatedSql);
        List<AvailableTool> tools = queryRepository.search(validatedSql);
        return new ToolAskResponse(tools);
    }
}
