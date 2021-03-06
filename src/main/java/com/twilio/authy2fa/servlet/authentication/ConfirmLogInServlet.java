package com.twilio.authy2fa.servlet.authentication;

import com.twilio.authy2fa.lib.SessionManager;
import com.twilio.authy2fa.models.User;
import com.twilio.authy2fa.models.UserService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/confirm-login"})
public class ConfirmLogInServlet extends HttpServlet {

    private final SessionManager sessionManager;
    private final UserService userService;

    @SuppressWarnings("unused")
    public ConfirmLogInServlet() {
        this(new SessionManager(), new UserService());
    }

    public ConfirmLogInServlet(SessionManager sessionManager, UserService userService) {
        this.sessionManager = sessionManager;
        this.userService = userService;
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long userId = sessionManager.getLoggedUserId(request);
        User user = userService.find(userId);

        String authyStatus = user.getAuthyStatus();

        // Reset the Authy status
        user.setAuthyStatus("");
        userService.update(user);

        switch (authyStatus) {
            case "approved":
                sessionManager.logIn(request, user.getId());
                response.sendRedirect("/account");
                break;
            case "denied":
                sessionManager.logOut(request);
                request.setAttribute("data", "You have declined the request");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
                break;
            default:
                sessionManager.logOut(request);
                request.setAttribute("data", "Unauthorized access");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
                break;
        }
    }
}
