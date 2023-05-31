package project.newchat.friend.service;

import static project.newchat.common.type.ErrorCode.ALREADY_FRIEND;
import static project.newchat.common.type.ErrorCode.NOT_FOUND_FRIEND_USER;
import static project.newchat.common.type.ErrorCode.NOT_FOUND_USER;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.newchat.common.exception.CustomException;
import project.newchat.common.type.ErrorCode;
import project.newchat.friend.domain.Friend;
import project.newchat.friend.dto.FriendDto;
import project.newchat.friend.repository.FriendRepository;
import project.newchat.user.domain.User;
import project.newchat.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

  private final UserRepository userRepository;
  private final FriendRepository friendRepository;

  @Override
  @Transactional
  public void addFriend(Long toUserId, Long myUserId) {

    User friend = userRepository.findById(toUserId)
        .orElseThrow(() -> new CustomException(NOT_FOUND_USER));
    User mySelf = userRepository.findById(myUserId)
        .orElseThrow(() -> new CustomException(NOT_FOUND_USER));
    Friend isFriendCheck = friendRepository.findByUserIdAndToUserId(mySelf.getId(), friend.getId());
    Long currentFriendNum = getCurrentFriendNum(myUserId);
    if (currentFriendNum >= 50) {
      throw new CustomException(ErrorCode.FRIEND_LIST_IS_FULL);
    }
    if (isFriendCheck == null) { // 처음 친구 신청한 상태

      Friend me = Friend.builder().user(mySelf).toUserId(friend.getId()).isFriend(true).build();

      Friend fri = Friend.builder().user(friend).toUserId(mySelf.getId()).isFriend(false).build();

      friendRepository.save(me);
      friendRepository.save(fri);

    } else {
      if (isFriendCheck.getIsFriend()) {
        throw new CustomException(ALREADY_FRIEND);
      }
    }
  }

  @Override
  @Transactional
  public void receiveFriend(Long toUserId, Long myUserId) {
    findUser(toUserId, myUserId);
    Friend receiver = friendRepository // 2,   1,  false
        .findByUserIdAndToUserIdAndIsFriend(myUserId, toUserId, false)
        .orElseThrow(() -> new CustomException(NOT_FOUND_USER));
    receiver.setIsFriend(true);
    friendRepository.save(receiver);
  }

  @Override
  @Transactional
  public void cancelFriend(Long toUserId, Long myUserId) {
    findUser(toUserId, myUserId);
    Friend mySelf = friendRepository.findByUserIdAndToUserIdAndIsFriend(
            myUserId, toUserId, true)
        .orElseThrow(() -> new CustomException(NOT_FOUND_FRIEND_USER));
    Friend friendSelf = friendRepository.findByUserIdAndToUserIdAndIsFriend(
            toUserId, myUserId, false)
        .orElseThrow(() -> new CustomException(NOT_FOUND_FRIEND_USER));
    friendRepository.deleteById(mySelf.getId());
    friendRepository.deleteById(friendSelf.getId());
  }

  @Override
  @Transactional
  public void refuseFriend(Long toUserId, Long myUserId) {
    findUser(toUserId, myUserId);
    Friend mySelf = friendRepository.findByUserIdAndToUserIdAndIsFriend(
            toUserId, myUserId, true)
        .orElseThrow(() -> new CustomException(NOT_FOUND_FRIEND_USER));
    Friend friendSelf = friendRepository.findByUserIdAndToUserIdAndIsFriend(
            myUserId, toUserId, false)
        .orElseThrow(() -> new CustomException(NOT_FOUND_FRIEND_USER));
    friendRepository.deleteById(mySelf.getId());
    friendRepository.deleteById(friendSelf.getId());
  }

  @Override
  @Transactional(readOnly = true)
  public List<FriendDto> getFriendList(Long myUserId, Pageable pageable) {
    userRepository.findById(myUserId)
        .orElseThrow(() -> new CustomException(NOT_FOUND_USER));
    List<Friend> myFriendList = friendRepository.findFriendByUserId(myUserId, pageable);
    List<FriendDto> friendDtoList = new ArrayList<>();
    for (Friend friend : myFriendList) {

      User fri = userRepository
          .findById(friend.getUser().getId())
          .orElseThrow(() -> new CustomException(NOT_FOUND_USER));

      friendDtoList.add(FriendDto.builder()
          .nickname(fri.getNickname())
          .build());
    }
    return friendDtoList;
  }

  @Override
  @Transactional(readOnly = true)
  public Long getCurrentFriendNum(Long userId) {
    return friendRepository.countByUserId(userId);
  }

  @Override
  @Transactional
  public void unfriend(Long toUserId, Long myUserId) {
    findUser(toUserId, myUserId);

    Friend meIsFriendCheck = friendRepository
        .findByUserIdAndToUserId(myUserId, toUserId);
    Friend friendIsFriendCheck = friendRepository
        .findByUserIdAndToUserId(toUserId, myUserId);

    if (meIsFriendCheck == null || friendIsFriendCheck == null) {
      throw new CustomException(NOT_FOUND_USER);
    }

    friendRepository.deleteById(meIsFriendCheck.getId());
    friendRepository.deleteById(friendIsFriendCheck.getId());
  }

  private void findUser(Long toUserId, Long myUserId) {
    userRepository.findById(toUserId)
        .orElseThrow(() -> new CustomException(NOT_FOUND_USER));
    userRepository.findById(myUserId)
        .orElseThrow(() -> new CustomException(NOT_FOUND_USER));
  }
}
