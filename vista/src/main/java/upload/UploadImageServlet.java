package upload;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;

@WebServlet(name = "UploadImageServlet", urlPatterns = "/upload-image")
@MultipartConfig(maxFileSize = 10 * 1024 * 1024, maxRequestSize = 20 * 1024 * 1024)
public class UploadImageServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Part part = req.getPart("imagen");
        if (part != null && part.getSize() > 0) {
            byte[] bytes = part.getInputStream().readAllBytes();
            String ct = part.getContentType();
            HttpSession session = req.getSession(true);
            session.setAttribute("uploadBytes", bytes);
            session.setAttribute("uploadMime", ct);
        }
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
