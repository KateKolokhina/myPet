package com.naukma.practice.myPet.controllers;

import com.naukma.practice.myPet.db.*;
import com.naukma.practice.myPet.db.DTO.OwnerDTO;
import com.naukma.practice.myPet.db.entity.*;
import javassist.NotFoundException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.sql.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequestMapping("owner")
public class OwnerController {

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ContractRepository contractRepository;

    @GetMapping(path = {"/profile"})
    public String ownerProfilePage(Model model, HttpServletRequest request) {

        String login = (String) request.getSession().getAttribute("userLogin");
        Owner owner = ownerRepository.findOwnerByLogin(login).get();
        User user = userRepository.findUserByLogin(login).get();
        model.addAttribute("ownerInfo", OwnerDTO.createOwner(owner, user));
        return "owner-profile";
    }

    @GetMapping(path = {"/profile/edit"})
    public String ownerProfileEditPage() {
        log.info("owner profile edit");
        return "owner-profile-edit";
    }

    @GetMapping(path = {"/posts"})
    public String ownerPostsPage(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "3") int size,
                                 @RequestParam(defaultValue = "0", name = "animal") String animal,
                                 @RequestParam(defaultValue = "1", name = "maxDays") String maxDaysId,
                                 Model model, HttpServletRequest request) throws Exception {

        String login = (String) request.getSession().getAttribute("userLogin");
        Owner owner = ownerRepository.findOwnerByLogin(login).get();

        String city = owner.getCity();
        String country = owner.getCountry();

        if (page > 0) {
            page -= 1;
        }
        long animalId = Long.parseLong(animal);
        int maxDays = Integer.parseInt(maxDaysId);

        try {
            List<Post> posts;
            Pageable paging = PageRequest.of(page, size);

            Page<Post> pagePosts = null;

            if (animalId != 0 && maxDays != 0) {
                pagePosts = postRepository.findDistinctByAnimalIdAndMaxDaysGreaterThanEqual(animalId, maxDays, paging);
            } else if (animalId != 0) {
                pagePosts = postRepository.findDistinctByAnimalId(animalId, paging);
            } else if (maxDays != 0) {
                pagePosts = postRepository.findByMaxDaysGreaterThanEqual(maxDays, paging);
            } else {
                pagePosts = postRepository.findAll(paging);
            }

            posts = pagePosts.getContent();
            posts = posts
                    .stream()
                    .filter(p -> p.getHost().getCity().equals(city)
                            && p.getHost().getCountry().equals(country))
                    .collect(Collectors.toList());

            if (posts.size() == 0) {
                model.addAttribute("message", "Oops. No results founded ...");
            } else {
                model.addAttribute("posts", posts);
            }

            model.addAttribute("currentPage", page + 1);
            model.addAttribute("animals", animalRepository.findAll());
            model.addAttribute("animal", animal);
            model.addAttribute("maxDays", maxDays);
            model.addAttribute("totalItems", pagePosts.getTotalElements());
            model.addAttribute("totalPages", pagePosts.getTotalPages());

        } catch (Exception e) {
            //TODO add custom exception
            throw new Exception("ERROR");
        }

        return "owner-posts";
    }


    @GetMapping(path = {"/posts/{id}"})
    public String ownerPostsIdPage(@PathVariable Long id, Model model) throws NotFoundException {
        Post post;
        if (postRepository.findById(id).isPresent()) {
            post = postRepository.findById(id).get();
        } else {
            //TODO add custom exception
            throw new NotFoundException("Post with this id doesn't exist");
        }
        model.addAttribute("postInfo", post);
        return "owner-posts-id";
    }


    @GetMapping(path = {"/createContract/{id}"})
    public String ownerCreateContractPage(@PathVariable Long id, Model model) throws NotFoundException {
        log.info("owner create contract " + id);
        Post post;
        if (postRepository.findById(id).isPresent()) {
            post = postRepository.findById(id).get();
        } else {
            //TODO add custom exception
            throw new NotFoundException("Error. Contract for not existing post! ");
        }
        model.addAttribute("postInfo", post);
        return "owner-create-contract";
    }

    private String dateToFormat(String date) {

        //05/31/2021
        //2019-01-26
        Pattern pattern = Pattern.compile("\\d{2,}");
        Matcher matcher = pattern.matcher(date);

        matcher.find();
        String day = matcher.group();
        matcher.find();
        String month = matcher.group();
        matcher.find();
        String year = matcher.group();

        return new StringBuilder(year).append('-').append(day).append('-').append(month).toString();
    }

    private boolean countInter(List<Contract> list, Date start, Date end, int maxInter) {
        int i = 0;
        for (Contract c : list) {
            if ((c.getStartDate().compareTo(start) <= 0 && c.getEndDate().compareTo(start) >= 0) ||
                    (c.getStartDate().compareTo(end) <= 0 && c.getEndDate().compareTo(end) >= 0) ||
                    (c.getStartDate().compareTo(start) >= 0 && c.getStartDate().compareTo(end) <= 0)) i++;

            if (i > maxInter - 1) {
                System.out.println("i: " + i);
                return false;
            }

        }
        return true;
    }

    //TODO CORRECT RETURN TYPE,ADD STATUS WINDOW
    @SneakyThrows
    @PostMapping(path = {"/createContract/{id}"})
    public void ownerCreateContractAction(@RequestParam(name = "startDate") String startDate,
                                          @RequestParam(name = "endDate") String endDate,
                                          @PathVariable Long id,
                                          Model model,HttpServletRequest request) throws NotFoundException {
        log.info(Date.valueOf(dateToFormat(startDate)) + " - " + Date.valueOf(dateToFormat(endDate)));
        Post post;
        if (postRepository.findById(id).isPresent()) {
            post = postRepository.findById(id).get();
        } else {
            //TODO add custom exception
            throw new NotFoundException("Error. Contract for not existing post! ");
        }

        Date start = Date.valueOf(dateToFormat(startDate));
        Date end = Date.valueOf(dateToFormat(endDate));
        Host host = post.getHost();
        //TODO check days number
        List<Contract> list = contractRepository.findAllDistinctByHostAndEndDateAfterOrStartDateBefore(host, start, end);
        System.out.println(list.toString());

        if (countInter(list, start, end, host.getMaxAnimals())) {
            String login = (String) request.getSession().getAttribute("userLogin");
            Owner owner = ownerRepository.findOwnerByLogin(login).get();
            Contract contract = Contract.createContract(post,owner,start, end);
            Contract result = contractRepository.save(contract);
            System.out.println(result);
        } else{
            //TODO add custom exception (errorSchedule)
            throw new Exception("Maximum number of animals for this host. Please, select other dates.");
        }

        //model.addAttribute("postInfo", post);
//        return "owner-create-contract";
    }


    @GetMapping(path = {"/contracts"})
    public String ownerContractsPage(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "3") int size,
                                     Model model, HttpServletRequest request) throws Exception {
        String login = (String) request.getSession().getAttribute("userLogin");
        if (page > 0) {
            page -= 1;
        }
        try {
            List<Contract> contracts;
            Pageable paging = PageRequest.of(page, size);

            Page<Contract> pageContracts = null;
            pageContracts = contractRepository.findAllByOwnerLogin(login,paging);

            contracts = pageContracts.getContent();
            if (contracts.size() == 0) {
                model.addAttribute("message", "Oops. No contracts...");
            } else {
                model.addAttribute("contractsList", contracts);
            }
            model.addAttribute("currentPage", page + 1);
            model.addAttribute("totalItems", pageContracts.getTotalElements());
            model.addAttribute("totalPages", pageContracts.getTotalPages());

        } catch (Exception e) {
            //TODO add custom exception
            throw new Exception("ERROR");
        }
        return "owner-contracts";
    }

    @GetMapping(path = {"/contracts/{id}"})
    public String ownerContractsIdPage(@PathVariable int id) {
        log.info("owner contracts " + id);
        return "owner-contracts-id";
    }

}