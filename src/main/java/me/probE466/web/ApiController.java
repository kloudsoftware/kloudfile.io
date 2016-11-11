package me.probE466.web;

import com.google.gson.Gson;
import me.probE466.persistence.entities.File;
import me.probE466.persistence.entities.User;
import me.probE466.persistence.repos.UserRepository;
import me.probE466.persistence.services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;

@Controller
public class ApiController {

    private final Gson GSON = new Gson();
    @Autowired
    private FileService fileService;
    @Autowired
    private UserRepository userRepository;

    @RequestMapping(value = "/api/list", method = RequestMethod.GET, produces = "text/plain")
    public
    @ResponseBody
    String getList(@RequestHeader("Authorization") String apiKey, @RequestHeader(required = false, value = "start") Integer start,
                   @RequestHeader(required = false, value = "limit") Integer limit) {
        User user = userRepository.findByUserKey(apiKey).orElseThrow(EntityNotFoundException::new);
        List<FileDTO> returnList = new ArrayList<>();
        List<File> fileList = user.getFileList();
        if (start == null && limit == null) {
            for (File file : fileList) {
                String ext;

                try {
                    ext = fileService.getExt(file.getFileName());
                } catch (InputMismatchException e) {
                    ext = "";
                }

                returnList.add(new FileDTO(file.getId(), file.getFileUrl(), file.getFileDeleteUrl(), file.getIsImage(),
                        ext, file.getFileName(), file.getFileDateCreated(), file.getFileDateUpdated(), file.getFileViewed()));
            }
        } else if (start == null && limit != 0 && limit > 0) {
            int currentIndex = 0;

            for (File file : fileList) {
                if (currentIndex == limit) {
                    break;
                }
                String ext;

                try {
                    ext = fileService.getExt(file.getFileName());
                } catch (InputMismatchException e) {
                    ext = "";
                }

                returnList.add(new FileDTO(file.getId(), file.getFileUrl(), file.getFileDeleteUrl(), file.getIsImage(),
                        ext, file.getFileName(), file.getFileDateCreated(), file.getFileDateUpdated(), file.getFileViewed()));
                currentIndex++;
            }
        } else if (start != null && start > 0 && limit != 0) {
            if (start > fileList.size()) {
                return "[]";
            }

            for (int i = start; i < start + limit; i++) {
                if (i > fileList.size() - 1) {
                    break;
                }

                File file = fileList.get(i);

                String ext;

                try {
                    ext = fileService.getExt(file.getFileName());
                } catch (InputMismatchException e) {
                    ext = "";
                }

                returnList.add(new FileDTO(file.getId(), file.getFileUrl(), file.getFileDeleteUrl(), file.getIsImage(),
                        ext, file.getFileName(), file.getFileDateCreated(), file.getFileDateUpdated(), file.getFileViewed()));

            }

        }
        return GSON.toJson(returnList);
    }
}
