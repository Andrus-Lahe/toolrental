package ee.toolrental.controller.category;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor

public class CategoryController {


    @GetMapping("/categories/detailed-info")
    @Operation(summary = "Kategooriate detailinfo päring")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
                            @ApiResponse(responseCode = "500", description = "Kategooriate laadimine ebaõnnestus")
    })
    public void  getCategoriesInfo(){


    }



}
