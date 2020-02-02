package com.jsonUtility.jsonCreator.controllers;

import com.amazonaws.HttpMethod;
import com.jsonUtility.jsonCreator.Dto.UserDto;
import com.jsonUtility.jsonCreator.JsonCreatorApplication;
import com.jsonUtility.jsonCreator.model.FileVersion;
import com.jsonUtility.jsonCreator.model.HRefModel;
import com.jsonUtility.jsonCreator.services.AWSServices;
import com.jsonUtility.jsonCreator.services.FileSystemStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.ws.rs.QueryParam;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.amazonaws.HttpMethod.GET;

@Controller
public class UiControllers {
    private  static String title = "jsonGenerator.com";
    public static String adminEmail = "admin@jsonGenerator.com";
    public static String adminPassword = "passw0rd";

    private AWSServices awsServices;

    @Autowired
    private UiControllers(AWSServices awsServices){this.awsServices = awsServices;}

    @Autowired
    private FileSystemStorageService fileSystemStorageService;

    @RequestMapping(value = "/login")
    public String login(Model model , @QueryParam("auth") @DefaultValue("0") Integer auth){
        model.addAttribute("title",title);
        model.addAttribute("userDto", new UserDto());
        return "login";
    }

    @RequestMapping(value = "/upload", method = RequestMethod.GET)
    public String uploadFiles(Model model){
        model.addAttribute("title",title);
        return "upload";
    }

    @PostMapping("/loginUser")
    public String loginUser(@ModelAttribute UserDto userDto){
        System.out.println(userDto.getEmail());
        if (adminEmail.equals(userDto.getEmail()) && adminPassword.equals(userDto.getPassword()))
            return "redirect:/upload";
        return "redirect:/login?auth=1";
    }

    @RequestMapping(value = "/files/list", method = RequestMethod.GET)
    public String listFiles(Model model) {
        List<Path> lodf = new ArrayList<>();
        List<HRefModel> uris = new ArrayList<>();
        try {
            /*lodf = fileSystemStorageService.listSourceFiles(fileSystemStorageService.getUploadLocation());
            for(Path pt : lodf) { // This is used when files were read from the physical location
                HRefModel href = new HRefModel();
                href.setHref(MvcUriComponentsBuilder
                        .fromMethodName(UiControllers.class, "serveFile", pt.getFileName().toString())
                        .build()
                        .toString());

                href.setHrefText(pt.getFileName().toString());
                uris.add(href);
            }*/
            List<FileVersion> fileVersions = fileSystemStorageService.getAllStoredFiles();
            fileVersions.forEach(fileVersion -> {
                HRefModel href = new HRefModel();
                href.setHrefText(fileVersion.getFileName());
                href.setHref(awsServices.generatePresignedUrl(fileVersion.getFileName(), GET,"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet").toString());
                uris.add(href);
            });
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        model.addAttribute("listOfEntries", uris);
        return "file_list :: urlFileList";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        Resource file = fileSystemStorageService.loadAsResource(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    @RequestMapping(value = "/delete/file/{filename:.+}", method = RequestMethod.GET)
    public String deleteFile(@PathVariable String filename,RedirectAttributes redirectAttributes) {
        if(fileSystemStorageService.deleteResource(filename))
            redirectAttributes.addFlashAttribute("message", "You successfully deleted " + filename + "!");
        else
            redirectAttributes.addFlashAttribute("message", "Error in file delete please try again deleting " + filename + "!");
        //restart();
        return "redirect:/upload";
    }

    @RequestMapping(value = "/files/upload", method = RequestMethod.POST)
    public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        fileSystemStorageService.store(file);
        redirectAttributes.addFlashAttribute("message", "You successfully uploaded " + file.getOriginalFilename() + "!");
        //restart();
        return "redirect:/upload";
    }

    /*public void restart(){
        Thread restartThread = new Thread(() -> {
            try {
                Thread.sleep(1000);
                JsonCreatorApplication.restart();
            } catch (InterruptedException ignored) {
            }
        });
        restartThread.setDaemon(false);
        restartThread.start();
    }*/
}
