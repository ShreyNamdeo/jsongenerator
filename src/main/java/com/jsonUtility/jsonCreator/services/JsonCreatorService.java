package com.jsonUtility.jsonCreator.services;

import com.jsonUtility.jsonCreator.Dto.FileVersionDto;
import com.jsonUtility.jsonCreator.Dto.JsonFileDto;
import com.jsonUtility.jsonCreator.model.FileVersion;
import com.jsonUtility.jsonCreator.repositories.FileVersionsRepository;
import com.jsonUtility.jsonCreator.util.Constants;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class JsonCreatorService {

    /*@Autowired
    ResourceLoader resourceLoader;*/

    AWSServices awsServices;

    @Autowired
    JsonCreatorService(AWSServices awsServices){
        this.awsServices = awsServices;
    }

    @Autowired
    private FileVersionsRepository fileVersionsRepository;

    private Path uploadLocation;

    @PostConstruct
    public void init() {
        this.uploadLocation = Paths.get(Constants.UPLOAD_LOCATION);
        try {
            Files.createDirectories(uploadLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    //private final String fileLocation = "static/ExcelFiles/";

    public FileVersionDto getJsonForSheet(String workbookName,String sheetName) {
        FileVersionDto fileVersionDto = new FileVersionDto();
        Optional<FileVersion> fileVersion = fileVersionsRepository.findByFileName(workbookName);
        if (fileVersion.isPresent()){
            FileVersion fv = fileVersion.get();
            fileVersionDto.setVersion(String.valueOf(fv.getFileVersion()));
            fileVersionDto.setCreated(fv.getCreatedDate());
            fileVersionDto.setUpdated(fv.getUpdatedDate());
            if (!sheetName.equalsIgnoreCase("Religion"))
                fileVersionDto.setData(processJsonForSheetsExceptReligion(workbookName,sheetName));
            else
                fileVersionDto.setData(processJsonForSheetReligion(workbookName,sheetName));
        }
        return fileVersionDto;

    }

    private List<JsonFileDto> processJsonForSheetsExceptReligion(String workbookName,String sheetName){
        List<JsonFileDto> jsonFileDtos= new ArrayList<>();
        try {
            //File file = ResourceUtils.getFile("classpath:static/ExcelFiles/"+workbookName);//2020_App_dates.xlsx
            //Resource resource = new ClassPathResource(fileLocation+workbookName);
            //Resource resource = getResourceByFileName(workbookName);
            InputStream input = awsServices.getFileInputStreamByKey(workbookName);
            File file = new File(workbookName);
            copyInputStreamToFile(input, file);
            FileInputStream inputStream = new FileInputStream(file);
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheet(sheetName);
            int rowCount = sheet.getLastRowNum()-sheet.getFirstRowNum();
            for (int i = 0; i < rowCount+1; i++) {
                Row row = sheet.getRow(i);
                JsonFileDto jsonFileDto = new JsonFileDto();
                //Create a loop to print cell values in a row
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    String val = "";
                    if (row.getCell(j) != null){
                        if (row.getCell(j).getCellType().name().equals("NUMERIC")) {
                            //val = String.valueOf(row.getCell(j).getNumericCellValue());
                            if (j == 3 || j == 5){
                                Date date = row.getCell(j).getDateCellValue();
                                DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
                                val = df.format(date);
                            }else
                                val = String.valueOf(row.getCell(j).getNumericCellValue());
                            //System.out.print(row.getCell(j).getNumericCellValue() + "|| ");
                        }
                        //Print Excel data in console
                        else if (row.getCell(j).getCellType().name().equals("BOOLEAN")) {
                            val = String.valueOf(row.getCell(j).getBooleanCellValue());
                            //System.out.print(row.getCell(j).getBooleanCellValue() + "|| ");
                        }
                        else {
                            val = row.getCell(j).getStringCellValue();
                            //System.out.print(row.getCell(j).getStringCellValue() + "|| ");
                        }
                    }
                    //Logic to add the row data into list
                    if (j == 0 && !val.isEmpty())
                        jsonFileDto.setId(val);
                    if (j==1)
                        jsonFileDto.setCategoriesSpecific((val));
                    if (j==2)
                        jsonFileDto.setSubject(val);
                    if (j==3)
                        jsonFileDto.setStartDate(val);
                    if (j==4)
                        jsonFileDto.setStartTime(val);
                    if (j==5)
                        jsonFileDto.setEndDate(val);
                    if (j==6)
                        jsonFileDto.setEndTime(val);
                    if (j==7)
                        jsonFileDto.setAlldayevent(val);
                    if (j==8)
                        jsonFileDto.setCategories(val);
                    if (j==9)
                        jsonFileDto.setWikipediaURL(val);
                    if (j==10)
                        jsonFileDto.setGreetingCardURL(val);
                    if (j==11)
                        jsonFileDto.setDescription(val);
                    if (j==12)
                        jsonFileDto.setCountries(val);
                }

                //if(isNotNull(jsonFileDto))

                if ( jsonFileDto.getId() != null && !( jsonFileDto.getId().equals("")
                        //&& jsonFileDto.getCategoriesSpecific().equals("")
                        //&& jsonFileDto.getSubject().equals("")
                        //&& jsonFileDto.getStartDate().equals("")
                        //&& jsonFileDto.getStartTime().equals("")
                        //&& jsonFileDto.getEndDate().equals("")
                        //&& jsonFileDto.getEndTime().equals("")
                        //&& jsonFileDto.getAlldayevent().equals("")
                        //&& jsonFileDto.getCategories().equals("")
                        //&& jsonFileDto.getWikipediaURL().equals("")
                        //&& jsonFileDto.getGreetingCardURL().equals("")
                        //&& jsonFileDto.getDescription().equals("")
                    )){
                    //if (row.getLastCellNum() != -1)
                    jsonFileDtos.add(jsonFileDto);
                }
                /*if (row.getLastCellNum() != -1)
                    jsonFileDtos.add(jsonFileDto);*/
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return jsonFileDtos;
    }

    private List<JsonFileDto> processJsonForSheetReligion(String workbookName,String sheetName){
        List<JsonFileDto> jsonFileDtos= new ArrayList<>();
        try {
            //File file = ResourceUtils.getFile("classpath:./static/ExcelFiles/"+workbookName);//2020_App_dates.xlsx

            //Resource resource = getResourceByFileName(workbookName);
            InputStream input = awsServices.getFileInputStreamByKey(workbookName);
            File file = new File(workbookName);
            copyInputStreamToFile(input, file);
            FileInputStream inputStream = new FileInputStream(file);
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheet(sheetName);
            int rowCount = sheet.getLastRowNum()-sheet.getFirstRowNum();
            for (int i = 0; i < rowCount+1; i++) {
                Row row = sheet.getRow(i);
                JsonFileDto jsonFileDto = new JsonFileDto();
                //Create a loop to print cell values in a row
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    String val = "";
                    if (row.getCell(j) != null){
                        if (row.getCell(j).getCellType().name().equals("NUMERIC")) {
                            if (j == 4 || j == 6){
                                Date date = row.getCell(j).getDateCellValue();
                                DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
                                val = df.format(date);
                            }else
                                val = String.valueOf(row.getCell(j).getNumericCellValue());
                            //System.out.print(row.getCell(j).getNumericCellValue() + "|| ");
                        }
                        //Print Excel data in console
                        else if (row.getCell(j).getCellType().name().equals("BOOLEAN")) {
                            val = String.valueOf(row.getCell(j).getBooleanCellValue());
                            //System.out.print(row.getCell(j).getBooleanCellValue() + "|| ");
                        }
                        else {
                            val = row.getCell(j).getStringCellValue();
                            //System.out.print(row.getCell(j).getStringCellValue() + "|| ");
                        }
                    }
                    //Logic to add the row data into list
                    if (j == 0 && !val.isEmpty())
                        jsonFileDto.setId(val);
                    if (j==1)
                        jsonFileDto.setCountries((val));
                    if (j==2)
                        jsonFileDto.setReligion((val));
                    if (j==3)
                        jsonFileDto.setSubject(val);
                    if (j==4)
                        jsonFileDto.setStartDate(val);
                    if (j==5)
                        jsonFileDto.setStartTime(val);
                    if (j==6)
                        jsonFileDto.setEndDate(val);
                    if (j==7)
                        jsonFileDto.setEndTime(val);
                    if (j==8)
                        jsonFileDto.setAlldayevent(val);
                    if (j==9)
                        jsonFileDto.setCategories(val);
                    if (j==10)
                        jsonFileDto.setWikipediaURL(val);
                    if (j==11)
                        jsonFileDto.setGreetingCardURL(val);
                    if (j==12)
                        jsonFileDto.setDescription(val);
                    if (j==13)
                        jsonFileDto.setCountries(val);
                }
                //if(isNotNullForReligion(jsonFileDto))
                    if (jsonFileDto.getId() != null && !( jsonFileDto.getId().equals("")
                            //&&jsonFileDto.getCountries().equals("")
                            //&& jsonFileDto.getReligion().equals("")
                            //&& jsonFileDto.getSubject().equals("")
                            //&& jsonFileDto.getStartDate().equals("")
                            //&& jsonFileDto.getStartTime().equals("")
                            //&& jsonFileDto.getEndDate().equals("")
                            //&& jsonFileDto.getEndTime().equals("")
                            //&& jsonFileDto.getAlldayevent().equals("")
                            //&& jsonFileDto.getCategories().equals("")
                            //&& jsonFileDto.getWikipediaURL().equals("")
                            //&& jsonFileDto.getGreetingCardURL().equals("")
                            //&& jsonFileDto.getDescription().equals("")
                    )){
                        //if (row.getLastCellNum() != -1)
                        jsonFileDtos.add(jsonFileDto);
                    }

                /*if (row.getLastCellNum() != -1)
                    jsonFileDtos.add(jsonFileDto);*/
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return jsonFileDtos;
    }

    public boolean isNotNull(JsonFileDto jsonFileDto) {
        return jsonFileDto.getCategoriesSpecific() != null
                && jsonFileDto.getSubject() != null
                && jsonFileDto.getStartDate() != null
                && jsonFileDto.getStartTime() != null
                && jsonFileDto.getEndDate() != null
                && jsonFileDto.getEndTime() != null
                && jsonFileDto.getAlldayevent() != null
                && jsonFileDto.getCategories() != null
                && jsonFileDto.getWikipediaURL() != null
                && jsonFileDto.getGreetingCardURL() != null
                && jsonFileDto.getDescription() != null;
    }

    public boolean isNotNullForReligion(JsonFileDto jsonFileDto) {
        return jsonFileDto.getCountries() != null
                && jsonFileDto.getReligion() != null
                && jsonFileDto.getSubject() != null
                && jsonFileDto.getStartDate() != null
                && jsonFileDto.getStartTime() != null
                && jsonFileDto.getEndDate() != null
                && jsonFileDto.getEndTime() != null
                && jsonFileDto.getAlldayevent() != null
                && jsonFileDto.getCountries() != null
                && jsonFileDto.getWikipediaURL() != null
                && jsonFileDto.getGreetingCardURL() != null
                && jsonFileDto.getDescription() != null;
    }

    public boolean isFileExist(String workbookName) {
        //Resource resource = resourceLoader.getResource("classpath:"+fileLocation+workbookName);
        /*Resource resource = getResourceByFileName(workbookName);
        return (resource.exists() || resource.isReadable());*/
        return awsServices.isFileWithNameExist(workbookName);
    }

    public Resource getResourceByFileName(String fileName){
        Path file = uploadLocation.resolve(fileName);
        Resource resource;
        try {
            resource = new UrlResource(file.toUri());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not read file: " + fileName, e);
        }
        return resource;
    }

    private static void copyInputStreamToFile(InputStream inputStream, File file)
            throws IOException {

        try (FileOutputStream outputStream = new FileOutputStream(file)) {

            int read;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }

            // commons-io
            //IOUtils.copy(inputStream, outputStream);

        }

    }
}
