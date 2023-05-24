package project.newchat.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.newchat.common.config.LoginCheck;
import project.newchat.common.type.ResponseMessage;
import project.newchat.common.util.ResponseUtils;
import project.newchat.user.domain.User;
import project.newchat.user.domain.request.LoginRequest;
import project.newchat.user.domain.request.TestUserRequest;
import project.newchat.user.domain.request.UserRequest;
import project.newchat.user.dto.UserDto;
import project.newchat.user.service.UserService;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat/user")
public class UserController {

  private final UserService userService;

  @PostMapping("/signUp")
  public ResponseEntity<Object> signUp(
      @RequestBody @Valid UserRequest userRequest) {
    UserDto user = userService.signUp(userRequest);
    return ResponseUtils.ok(ResponseMessage.CREATE_USER, user);
  }

  @PostMapping("/signUp2") // 서버 터트리기 테스트
  public ResponseEntity<Object> signUpTest(
      @RequestBody @Valid TestUserRequest userRequest) {
    UserDto user = userService.signUpTe2(userRequest);
    return ResponseUtils.ok(ResponseMessage.CREATE_USER, user);
  }

  @PostMapping("/login")
  public ResponseEntity<Object> login(
      @RequestBody @Valid LoginRequest userRequest,
      HttpSession session) {
    User login = userService.login(userRequest);
    session.setAttribute("user", login.getId());
    return ResponseUtils.ok(ResponseMessage.LOGIN_SUCCESS);
  }

  @PostMapping("/logout")
  @LoginCheck
  public ResponseEntity<Object> logout(HttpSession session) {
    session.invalidate();
    return ResponseUtils.ok(ResponseMessage.LOGOUT_SUCCESS);
  }
}
