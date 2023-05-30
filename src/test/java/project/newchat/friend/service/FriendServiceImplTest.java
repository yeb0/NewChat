package project.newchat.friend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import project.newchat.common.exception.CustomException;
import project.newchat.friend.domain.Friend;
import project.newchat.friend.domain.Friend.FriendBuilder;
import project.newchat.friend.repository.FriendRepository;
import project.newchat.user.domain.User;
import project.newchat.user.domain.request.UserRequest;
import project.newchat.user.repository.UserRepository;
import project.newchat.user.service.UserService;

@SpringBootTest
@Transactional
class FriendServiceImplTest {
  @Autowired
  private FriendService friendService;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private FriendRepository friendRepository;

  @Test
  @DisplayName("이미 친구신청한 상태이거나 친구인 상태일 경우")
  void already_friend_failed() {
    User test1 = User.builder()
        .id(1L)
        .email("test@e")
        .nickname("test")
        .createdAt(LocalDateTime.now())
        .build();
    User test2 = User.builder()
        .id(2L)
        .email("test2@e")
        .nickname("test")
        .createdAt(LocalDateTime.now())
        .build();
    userRepository.save(test1);
    userRepository.save(test2);

    Friend friend1 = Friend.builder()
        .id(1L)
        .user(test1)
        .toUserId(2L)
        .isFriend(true).build();
    Friend friend2 = Friend.builder()
        .id(2L)
        .user(test2)
        .toUserId(1L)
        .isFriend(false).build();

    friendRepository.save(friend1);
    friendRepository.save(friend2);

    assertThrows(CustomException.class, () -> {
      friendService.addFriend(2L, 1L);
    });
  }
}