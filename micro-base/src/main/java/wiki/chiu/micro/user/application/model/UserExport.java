package wiki.chiu.micro.user.application.model;

import java.util.List;

import wiki.chiu.micro.user.domain.User;

/**
 * The rows the user export delivers. Rendering them into a downloadable script belongs to the
 * adapter that offers the download.
 */
public record UserExport(List<User> users) {
}
