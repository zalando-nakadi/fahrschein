package org.zalando.spring.boot.fahrschein.nakadi.config.properties;

import java.util.ArrayList;
import java.util.List;

public class AuthorizationUserServiceTeamLists {

	private List<String> users = new ArrayList<>();
	private List<String> services = new ArrayList<>();
	private List<String> teams = new ArrayList<>();

	public AuthorizationUserServiceTeamLists(List<String> users, List<String> services, List<String> teams) {
		this.users = users;
		this.services = services;
		this.teams = teams;
	}

	public AuthorizationUserServiceTeamLists() {
	}

	public static AuthorizationUserServiceTeamLists create(List<String> users, List<String> services, List<String> teams) {
		return new AuthorizationUserServiceTeamLists(users, services, teams);
	}

	public List<String> getUsers() {
		return this.users;
	}

	public List<String> getServices() {
		return this.services;
	}

	public List<String> getTeams() {
		return this.teams;
	}

	public void setUsers(List<String> users) {
		this.users = users;
	}

	public void setServices(List<String> services) {
		this.services = services;
	}

	public void setTeams(List<String> teams) {
		this.teams = teams;
	}

	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof AuthorizationUserServiceTeamLists)) return false;
		final AuthorizationUserServiceTeamLists other = (AuthorizationUserServiceTeamLists) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$users = this.getUsers();
		final Object other$users = other.getUsers();
		if (this$users == null ? other$users != null : !this$users.equals(other$users)) return false;
		final Object this$services = this.getServices();
		final Object other$services = other.getServices();
		if (this$services == null ? other$services != null : !this$services.equals(other$services)) return false;
		final Object this$teams = this.getTeams();
		final Object other$teams = other.getTeams();
		if (this$teams == null ? other$teams != null : !this$teams.equals(other$teams)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof AuthorizationUserServiceTeamLists;
	}

	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $users = this.getUsers();
		result = result * PRIME + ($users == null ? 43 : $users.hashCode());
		final Object $services = this.getServices();
		result = result * PRIME + ($services == null ? 43 : $services.hashCode());
		final Object $teams = this.getTeams();
		result = result * PRIME + ($teams == null ? 43 : $teams.hashCode());
		return result;
	}

	public String toString() {
		return "AuthorizationUserServiceTeamLists(users=" + this.getUsers() + ", services=" + this.getServices() + ", teams=" + this.getTeams() + ")";
	}
}
