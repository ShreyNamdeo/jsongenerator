package com.jsonUtility.jsonCreator.controllers;

import com.jsonUtility.jsonCreator.services.JsonCreatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/json")
public class JsonCreatorController {

    @Autowired
    JsonCreatorService jsonCreatorService;

    @GetMapping("file/{workbookName}/sheet/{sheetName}")
    public ResponseEntity<Object> getFileToJson(@PathVariable("workbookName") String workbookName,
                                        @PathVariable("sheetName") String sheetName) {
        if (!jsonCreatorService.isFileExist(workbookName))
            return new ResponseEntity<>("Incorrect file name", HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(jsonCreatorService.getJsonForSheet(workbookName,sheetName), HttpStatus.OK);

    }
}
