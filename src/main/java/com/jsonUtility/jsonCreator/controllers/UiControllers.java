package com.jsonUtility.jsonCreator.controllers;

import com.jsonUtility.jsonCreator.Dto.UserDto;
import com.jsonUtility.jsonCreator.model.HRefModel;
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

@Controller
public class UiControllers {
    private  static String title = "jsonGenerator.com";
    public static String adminEmail = "admin@jsonGenerator.com";
    public static String adminPassword = "passw0rd";

    @Autowired
    private FileSystemStorageService storageService;

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
            lodf = storageService.listSourceFiles(storageService.getUploadLocation());
            for(Path pt : lodf) {
                HRefModel href = new HRefModel();
                href.setHref(MvcUriComponentsBuilder
                        .fromMethodName(UiControllers.class, "serveFile", pt.getFileName().toString())
                        .build()
                        .toString());

                href.setHrefText(pt.getFileName().toString());
                uris.add(href);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        model.addAttribute("listOfEntries", uris);
        return "file_list :: urlFileList";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    @RequestMapping(value = "/files/upload", method = RequestMethod.POST)
    public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        storageService.store(file);
        redirectAttributes.addFlashAttribute("message", "You successfully uploaded " + file.getOriginalFilename() + "!");
        return "redirect:/upload";
    }
}
