package com.naukma.practice.myPet.services;

import com.naukma.practice.myPet.db.ContractRepository;
import com.naukma.practice.myPet.db.HostRepository;
import com.naukma.practice.myPet.db.OwnerRepository;
import com.naukma.practice.myPet.db.UserRepository;
import com.naukma.practice.myPet.db.entity.Contract;
import com.naukma.practice.myPet.db.entity.Host;
import com.naukma.practice.myPet.db.entity.Owner;
import com.naukma.practice.myPet.db.entity.User;
import com.naukma.practice.myPet.exceptions.InvalidDataException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Service class with methods for authentication purposes
 */

@Service
@Slf4j
public class AuthenticationService implements AuthenticationServiceInterface {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private HostRepository hostRepository;
    @Autowired
    private OwnerRepository ownerRepository;
    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private static final String LOGIN_PATTERN =
            "^[a-zA-Z0-9]([._-](?![._-])|[a-zA-Z0-9]){3,18}[a-zA-Z0-9]$";
    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z]).{8,32}$";
    private static final String EMAIL_PATTERN = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";

    private static final String TELEPHONE_PATTERN = "^[+]*[(]{0,1}[0-9]{1,4}[)]{0,1}[-\\s\\./0-9]*$";

    /**
     * Checks identity of user
     * @param request where data contains
     * @param login of logged user
     * @return where to forward
     */
    @Override
    public String loginUser(String login,
                            HttpServletRequest request) throws Exception {

        String forward = null;
        HttpSession session = request.getSession();
        HttpSession checkSession = request.getSession(false);
        String checkUserRole = (String) checkSession.getAttribute("userRole");

        if (checkUserRole != null) {
            throw new AuthenticationException("User is already signed in");
        }

        log.info("Request parameter: login --> " + login);

        User user;
        if (userRepository.findUserByLogin(login).isPresent()) {
            user = userRepository.findUserByLogin(login).get();
            log.trace("Found in DB: user --> " + user);
        } else {
            throw new InvalidDataException("Cannot find user with such login");
        }

        String userRole = user.getRole();
        if (userRole.equals("HOST")) {
            forward = "/host/profile";
        } else if (userRole.equals("OWNER")) {
            forward = "/owner/profile";
        }
        setUserToSession(session, user, userRole);
        updateStatusesOfContracts(login, userRole);
        return request.getContextPath() + forward;
    }


    /**
     * Changes user's role in db
     * @param login user's login
     * @param userRole user's new role
     */
    public void updateStatusesOfContracts(String login, String userRole) {

        Date today = new Date(System.currentTimeMillis());
        List<Contract> contracts = null;
        if (userRole.equals("HOST")) {
            contracts = contractRepository.findAllByHostLogin(login);
        } else if (userRole.equals("OWNER")) {
            contracts = contractRepository.findAllByOwnerLogin(login);
        }
        if (contracts.size() == 0)
            return;
        for (Contract c : contracts) {

            Date contractStartDate = c.getStartDate();
            Date contractEndDate = c.getEndDate();

            String currentState = c.getStatus();

            String newState = c.getStatus();

            int todayToStart = today.compareTo(contractStartDate);
            int todayToEnd = today.compareTo(contractEndDate);
            System.out.println("compare- " + todayToStart + " : " + todayToEnd);
            if (today.after(contractStartDate) && today.after(contractEndDate)) {
                newState = "FINISHED";
            } else if (todayToStart >= 0 && todayToEnd <= 0) {
                newState = "ACTIVE";

            }

            if ((currentState.equals("RATED")) || currentState.equals("FINISHED") || currentState.equals("NEW") || currentState.equals("EMERGENCY")) {
                return;
            } else if (!c.getStatus().equalsIgnoreCase(newState)) {
                try {
                    contractRepository.updateStatus(c.getId(), newState);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * Register new user
     * @param request where data lies
     */
    @Override
    public void registrationUser(String login, String password, String email, String password_confirm,
                                 String name, String surname, String telephone, String country, String city, String role,
                                 HttpServletRequest request) throws Exception {

        HttpSession checkSession = request.getSession(false);
        String checkUserRole = (String) checkSession.getAttribute("userRole");
        log.trace("Session attribute: userRole --> " + checkUserRole);

        if (checkUserRole != null)
            throw new AuthenticationException("User is already signed in");

        log.info("Request parameter: login --> " + login);
        log.info("Request parameter: password --> " + password);
        log.info("Request parameter: email --> " + email);
        log.info("Request parameter: password_confirm --> " + password_confirm);

        log.info("Request parameter: name --> " + name);
        log.info("Request parameter: surname --> " + surname);
        log.info("Request parameter: telephone --> " + telephone);
        log.info("Request parameter: country --> " + country);
        log.info("Request parameter: city --> " + city);
        log.info("Request parameter: role --> " + role);

        if (!validateData(login, password, email, password_confirm,
                name, surname, telephone, country, city, role))
            throw new InvalidDataException("Input data is invalid or empty");

        if (userRepository.findUserByLogin(login).isPresent()
                || userRepository.findUserByEmail(email).isPresent()
                || hostRepository.findHostByPhone(telephone).isPresent()
                || ownerRepository.findOwnerByLogin(telephone).isPresent()
        ) {
            throw new InvalidDataException("User with this login, telephone or e-mail is already registered");
        } else {
            String bcryptHashString = passwordEncoder.encode(password);
            User insertUser = User.createUser(login, email, bcryptHashString, role);
            User resultUser = userRepository.save(insertUser);
            System.out.println("result for : add to users list ->" + resultUser);
            if (role.equals("HOST")) {
                Host insertHost = Host.createHost(login, name, surname, country, city, telephone, 0.0, 1);
                Host resultHost = hostRepository.save(insertHost);
                System.out.println("result for : add to hosts list ->" + resultHost);
            } else if (role.equals("OWNER")) {
                Owner insertOwner = Owner.createOwner(login, name, surname, country, city, telephone);
                Owner resultOwner = ownerRepository.save(insertOwner);
                System.out.println("result for : add to owners list ->" + resultOwner);
            }
        }
    }

    /**
     * Sets user to session
     * @param session session where to insert user
     * @param insertUser user to add to session
     * @param userRole role of user
     */
    static void setUserToSession(HttpSession session, User insertUser, String userRole) {
//      save only user id in session (for next queries)
        session.setAttribute("userLogin", insertUser.getLogin());
        session.setAttribute("userRole", userRole);
        log.info("User " + insertUser + " logged as " + userRole.toLowerCase());
    }


    /**
     * Checks if login and password valid
     * @param login user's login
     * @param password user's password
     */
    @Override
    public boolean validateData(String login, String password) {
        return login != null && password != null && !login.isEmpty() && !password.isEmpty();
    }


    /**
     * Checks if user's data is valid
     */
    @Override
    public boolean validateData(String login, String password, String email, String password_confirm,
                                String name, String surname, String telephone, String country, String city, String role) {
        if (!validateData(login, password)
                || email == null || email.isEmpty()
                || password_confirm == null || password_confirm.isEmpty()
                || name == null || name.isEmpty()
                || surname == null || surname.isEmpty()
                || telephone == null || telephone.isEmpty()
                || country == null || country.isEmpty()
                || city == null || city.isEmpty()
                || role == null || role.isEmpty()
        ) {
            return false;
        } else
            return (isValid(login, LOGIN_PATTERN)
                    && isValid(password, PASSWORD_PATTERN)
                    && isValid(email, EMAIL_PATTERN)
                    && isValid(telephone, TELEPHONE_PATTERN)
//
//                    && isValid(name, TEXT_PATTERN)
//                    && isValid(surname, TEXT_PATTERN)
//                    && isValid(country, TEXT_PATTERN)
//                    && isValid(city, TEXT_PATTERN)
                    && (role.equals("HOST") || role.equals("OWNER"))
            );
    }


    /**
     * Checks string by regex
     * @param value string to check
     * @param regex regex to check with
     */
    private static boolean isValid(final String value, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }
}
