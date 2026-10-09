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
            return new ToolAskResponse("Saan aidata ainult saadaval tööriistade otsimisega.", List.of());
        }

        ToolSearchSqlValidator.ValidatedSql validatedSql = sqlValidator.validate(generatedSql);
        List<AvailableTool> tools = queryRepository.search(validatedSql);
        String answer = tools.isEmpty()
                ? "Sellele otsingule vastavaid saadaval tööriistu ei leitud."
                : "Leidsin %d saadaval tööriista.".formatted(tools.size());
        return new ToolAskResponse(answer, tools);
    }
}
