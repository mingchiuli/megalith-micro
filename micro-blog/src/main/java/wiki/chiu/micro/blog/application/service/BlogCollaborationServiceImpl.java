package wiki.chiu.micro.blog.application.service;

import static wiki.chiu.micro.common.lang.ExceptionMessage.NO_FOUND;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import wiki.chiu.micro.blog.application.port.in.BlogCollaborationService;
import wiki.chiu.micro.blog.application.port.out.BlogQueryStore;
import wiki.chiu.micro.blog.application.port.out.BlogRuntimeStore;
import wiki.chiu.micro.blog.application.port.out.CollaborationTicketGateway;
import wiki.chiu.micro.blog.domain.BlogEntity;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.DataPermissionEnum;

@Service
public class BlogCollaborationServiceImpl implements BlogCollaborationService {

    private final BlogQueryStore blogs;
    private final BlogAccessPolicy accessPolicy;
    private final BlogRuntimeStore runtimeStore;
    private final CollaborationTicketGateway tickets;

    public BlogCollaborationServiceImpl(
        BlogQueryStore blogs,
        BlogAccessPolicy accessPolicy,
        BlogRuntimeStore runtimeStore,
        CollaborationTicketGateway tickets) {
        this.blogs = blogs;
        this.accessPolicy = accessPolicy;
        this.runtimeStore = runtimeStore;
        this.tickets = tickets;
    }

    @Override
    public String issueReadToken(Long blogId, Long userId, List<DataPermissionEnum> dataPermissions) {
        BlogEntity blog = blogs.findById(blogId).orElseThrow(() -> new MissException(NO_FOUND));
        accessPolicy.requireEdit(blog, userId, dataPermissions);
        String token = UUID.randomUUID().toString();
        runtimeStore.saveReadToken(blogId, token);
        return token;
    }

    @Override
    public String issueWebSocketTicket(
        Long blogId, Long userId, List<DataPermissionEnum> dataPermissions) {
        String roomId;
        if (blogId == null) {
            accessPolicy.requireAuthenticated(userId);
            roomId = "init:" + userId;
        } else {
            BlogEntity blog = blogs.findById(blogId).orElseThrow(() -> new MissException(NO_FOUND));
            accessPolicy.requireCollaboration(blog, userId, dataPermissions);
            roomId = blogId.toString();
        }
        return tickets.issueTicket(roomId);
    }
}
