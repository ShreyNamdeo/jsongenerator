package com.jsonUtility.jsonCreator.controllers;

import com.jsonUtility.jsonCreator.Dto.UserDto;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.ws.rs.QueryParam;
import java.util.Optional;

@Controller
public class UiControllers {
    private  static String title = "jsonGenerator.com";
    public static String adminEmail = "admin@jsonGenerator.com";
    public static String adminPassword = "passw0rd";


    @RequestMapping(value = "/login")
    public String login(Model model , @QueryParam("auth") @DefaultValue("0") Integer auth){
        model.addAttribute("title",title);
        model.addAttribute("userDto", new UserDto());
        return "login";
    }

    @PostMapping("/loginUser")
    public String loginUser(@ModelAttribute UserDto userDto){
        System.out.println(userDto.getEmail());
        if (adminEmail.equals(userDto.getEmail()) && adminPassword.equals(userDto.getPassword()))
            return "redirect:/adminProductList";
        return "redirect:/login?auth=1";
    }
}
