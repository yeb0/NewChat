package project.newchat.chatroom.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.newchat.chatmsg.domain.ChatMsg;
import project.newchat.user.domain.User;
import project.newchat.userchatroom.domain.UserChatRoom;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id")
    private Long id;

    private String title;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long roomCreator;

    private Integer userCountMax; // 최대 인원 8명


    @OneToMany(mappedBy = "chatRoom", fetch = FetchType.LAZY)
    private List<UserChatRoom> userChatRooms;

    @OneToMany(mappedBy = "chatRoom", fetch = FetchType.LAZY)
    private List<ChatMsg> chatMsgs;
}
