package org.zalando.spring.boot.fahrschein.nakadi.config.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationUserServiceTeamLists {

	private List<String> users = new ArrayList<>();
	private List<String> services = new ArrayList<>();
	private List<String> teams = new ArrayList<>();

	public static AuthorizationUserServiceTeamLists create(List<String> users, List<String> services, List<String> teams) {
		return new AuthorizationUserServiceTeamLists(users, services, teams);
	}
}
