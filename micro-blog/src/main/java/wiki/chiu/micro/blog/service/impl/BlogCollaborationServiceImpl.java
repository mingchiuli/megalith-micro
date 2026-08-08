package wiki.chiu.micro.blog.service.impl;

import static wiki.chiu.micro.common.lang.Const.READ_TOKEN;
import static wiki.chiu.micro.common.lang.ExceptionMessage.NO_FOUND;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Service;
import wiki.chiu.micro.blog.entity.BlogEntity;
import wiki.chiu.micro.blog.repository.BlogRepository;
import wiki.chiu.micro.blog.service.BlogAccessPolicy;
import wiki.chiu.micro.blog.service.BlogCollaborationService;
import wiki.chiu.micro.blog.service.port.CollaborationTicketGateway;
import wiki.chiu.micro.common.exception.MissException;

@Service
public class BlogCollaborationServiceImpl implements BlogCollaborationService {

  private final BlogRepository blogs;
  private final BlogAccessPolicy accessPolicy;
  private final StringRedisTemplate redisTemplate;
  private final CollaborationTicketGateway tickets;

  public BlogCollaborationServiceImpl(
      BlogRepository blogs,
      BlogAccessPolicy accessPolicy,
      StringRedisTemplate redisTemplate,
      CollaborationTicketGateway tickets) {
    this.blogs = blogs;
    this.accessPolicy = accessPolicy;
    this.redisTemplate = redisTemplate;
    this.tickets = tickets;
  }

  @Override
  public String issueReadToken(Long blogId, Long userId, List<String> roles) {
    BlogEntity blog = blogs.findById(blogId).orElseThrow(() -> new MissException(NO_FOUND));
    accessPolicy.requireManagement(blog, userId, roles);
    String token = UUID.randomUUID().toString();
    redisTemplate
        .opsForValue()
        .set(READ_TOKEN + blogId, token, Expiration.from(24, TimeUnit.HOURS));
    return token;
  }

  @Override
  public String issueWebSocketTicket(Long blogId, Long userId, List<String> roles) {
    String roomId;
    if (blogId == null) {
      accessPolicy.requireAuthenticated(userId);
      roomId = "init:" + userId;
    } else {
      BlogEntity blog = blogs.findById(blogId).orElseThrow(() -> new MissException(NO_FOUND));
      accessPolicy.requireCollaboration(blog, userId, roles);
      roomId = blogId.toString();
    }
    return tickets.issueTicket(userId, roomId);
  }
}
