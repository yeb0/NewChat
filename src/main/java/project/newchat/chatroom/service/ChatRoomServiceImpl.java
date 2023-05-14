package project.newchat.chatroom.service;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.newchat.chatmsg.repository.ChatMsgRepository;
import project.newchat.chatroom.controller.request.ChatRoomRequest;
import project.newchat.chatroom.domain.ChatRoom;
import project.newchat.chatroom.dto.ChatRoomDto;
import project.newchat.chatroom.repository.ChatRoomRepository;
import project.newchat.common.exception.CustomException;
import project.newchat.common.type.ErrorCode;
import project.newchat.user.domain.User;
import project.newchat.user.repository.UserRepository;
import project.newchat.userchatroom.domain.UserChatRoom;
import project.newchat.userchatroom.repository.UserChatRoomRepository;

@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {

  private final ChatRoomRepository chatRoomRepository;

  private final ChatMsgRepository chatMsgRepository;

  private final UserRepository userRepository;

  private final UserChatRoomRepository userChatRoomRepository;

  @Override
  @Transactional
  public void createRoom(ChatRoomRequest chatRoomRequest, Long userId) {
    // 유저정보조회
    User findUser = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));//
    // chatroom 생성
    ChatRoom chatRoom = ChatRoom.builder()
        .roomCreator(findUser.getId())
        .title(chatRoomRequest.getTitle())
        .userCountMax(chatRoomRequest.getUserCountMax())
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
    ChatRoom save = chatRoomRepository.save(chatRoom);

    // 연관관계 user_chat room 생성
    UserChatRoom userChatRoom = UserChatRoom.builder()
        .user(findUser)
        .chatRoom(save)
        .build();
    // save
    userChatRoomRepository.save(userChatRoom);
  }

  @Override
  @Transactional
  public void joinRoom(Long roomId, Long userId) {
    // 유저 조회
    User findUser = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

    // room 조회
    ChatRoom chatRoom = chatRoomRepository.findById(roomId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ROOM));

    // user_chatroom 현재 인원 카운트
    Long currentUserCount = userChatRoomRepository.countByChatRoomId(roomId);

    // chatroom 입장
    if (currentUserCount >= chatRoom.getUserCountMax()) {
      throw new CustomException(ErrorCode.ROOM_USER_FULL);
    }

    UserChatRoom userChatRoom = UserChatRoom.builder()
        .user(findUser)
        .chatRoom(chatRoom)
        .build();
    userChatRoomRepository.save(userChatRoom);

  }
  // 채팅방 전체 조회
  @Override
  @Transactional
  public List<ChatRoomDto> getRoomList(Pageable pageable) {
    Page<ChatRoom> all = chatRoomRepository.findAll(pageable);
    return getChatRoomDtos(all);
  }

  // 자신이 생성한 방 리스트 조회
  @Override
  public List<ChatRoomDto> roomsByCreatorUser(Long userId, Pageable pageable) {
    Page<ChatRoom> all = chatRoomRepository.findAllByUserId(userId, pageable);
    return getChatRoomDtos(all);
  }

  // 자신이 참여한 방 리스트 조회
  @Override
  public List<ChatRoomDto> getUserByRoomPartList(Long userId, Pageable pageable) {
    Page<ChatRoom> allByUserIdAndUserChatRooms = chatRoomRepository
        .findAllByUserChatRoomsUserId(userId, pageable);
    return getChatRoomDtos(allByUserIdAndUserChatRooms);
  }

  @Override
  @Transactional
  public void outRoom(Long userId, Long roomId) {
    Long roomCreatorId = chatRoomRepository
        .findChatRoomIdByRoomId(roomId);
    // 방장이 아니라면
    if (!Objects.equals(roomCreatorId, userId)) {
      userChatRoomRepository.deleteUserChatRoomByUserId(userId);
      return;
    }
    // 방장이라면 방 삭제
    userChatRoomRepository.deleteUserChatRoomByChatRoom_Id(roomId);
    chatRoomRepository.deleteById(roomId);
  }

  @Override
  @Transactional
  public void deleteRoom(Long userId, Long roomId) {
    Long roomCreatorId = chatRoomRepository
        .findChatRoomIdByRoomId(roomId);
    if (!Objects.equals(roomCreatorId, userId)) {
      throw new CustomException(ErrorCode.NOT_ROOM_CREATOR);
    }
    userChatRoomRepository.deleteUserChatRoomByChatRoom_Id(roomId);
    chatRoomRepository.deleteById(roomId);
    // todo : 채팅 메시지 구현 시, 방 삭제할 때 메시지도 같이 삭제되는 메서드 구현
//    chatMsgRepository.deleteById(roomId); 방 삭제 시 채팅도 다 삭제 되어야 함.
  }

  // 방 조회 DTO 변환 메서드 추출
  private static List<ChatRoomDto> getChatRoomDtos(Page<ChatRoom> all) {
//    List<ChatRoomDto> chatRoomList = new ArrayList<>();
//
//    for (ChatRoom list : all) {
//      ChatRoomDto dto = ChatRoomDto.builder()
//          .id(list.getId())
//          .title(list.getTitle())
//          .currentUserCount((long) list.getUserChatRooms().size())
//          .userCountMax(list.getUserCountMax())
//          .build();
//
//      chatRoomList.add(dto);
//    }
//    return chatRoomList;
    return all.stream()
        .map(ChatRoomDto::of)
        .collect(Collectors.toList());
  }
}
