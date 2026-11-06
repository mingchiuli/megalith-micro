package wiki.chiu.micro.blog.application.service;

import static wiki.chiu.micro.common.error.ExceptionMessage.NO_FOUND;

import java.util.List;
import java.util.UUID;

import wiki.chiu.micro.blog.application.model.CollaborationTicket;
import wiki.chiu.micro.blog.application.port.in.BlogCollaborationService;
import wiki.chiu.micro.blog.application.port.out.BlogQueryStore;
import wiki.chiu.micro.blog.application.port.out.BlogRuntimeStore;
import wiki.chiu.micro.blog.application.port.out.CollaborationRoomInitializer;
import wiki.chiu.micro.blog.application.port.out.CollaborationTicketGateway;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.common.enums.DataPermissionEnum;
import wiki.chiu.micro.common.error.ExceptionMessage;
import wiki.chiu.micro.common.exception.MissException;

public class BlogCollaborationServiceImpl implements BlogCollaborationService {

    private final BlogQueryStore blogs;
    private final BlogAccessPolicy accessPolicy;
    private final BlogRuntimeStore runtimeStore;
    private final CollaborationTicketGateway tickets;
    private final BlogEditor editor;
    private final CollaborationRoomInitializer rooms;

    public BlogCollaborationServiceImpl(
        BlogQueryStore blogs,
        BlogAccessPolicy accessPolicy,
        BlogRuntimeStore runtimeStore,
        CollaborationTicketGateway tickets,
        BlogEditor editor,
        CollaborationRoomInitializer rooms) {
        this.blogs = blogs;
        this.accessPolicy = accessPolicy;
        this.runtimeStore = runtimeStore;
        this.tickets = tickets;
        this.editor = editor;
        this.rooms = rooms;
    }

    @Override
    public String issueReadToken(Long blogId, Long userId, List<DataPermissionEnum> dataPermissions) {
        Blog blog = blogs.findById(blogId).orElseThrow(() -> new MissException(NO_FOUND));
        accessPolicy.requireEdit(blog, userId, dataPermissions);
        String token = UUID.randomUUID().toString();
        runtimeStore.saveReadToken(blogId, token);
        return token;
    }

    @Override
    public CollaborationTicket issueWebSocketTicket(
        Long blogId, Long userId, List<DataPermissionEnum> dataPermissions, long expiresAt) {
        if (blogId == null || expiresAt <= System.currentTimeMillis()
            || expiresAt > System.currentTimeMillis() + 60_000) {
            throw new IllegalArgumentException("invalid collaboration lease");
        }
        var edit = editor.loadEdit(blogId, userId, dataPermissions, true);
        if (!edit.permissions().collaborate()) {
            throw new MissException(ExceptionMessage.EDIT_NO_AUTH);
        }
        String roomId = "blog:" + blogId + ":" + edit.collaborationEpoch();
        rooms.initialize(roomId, edit.content());
        // JWT timestamps have second precision; expose the same non-extending deadline.
        long deadline = expiresAt / 1000 * 1000;
        return new CollaborationTicket(
            tickets.issueTicket(roomId, deadline), roomId, edit.collaborationEpoch(), deadline);
    }
}
