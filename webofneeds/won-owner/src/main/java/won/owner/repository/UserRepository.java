/*
 * This file is subject to the terms and conditions defined in file 'LICENSE.txt', which is part of this source code package.
 */

package won.owner.repository;

import org.springframework.data.jpa.repository.Query;
import won.owner.model.User;
import won.protocol.repository.WonRepository;

import java.net.URI;

/**
 * User: t.kozel
 * Date: 11/7/13
 */
public interface UserRepository extends WonRepository<User> {

	public User findByUsername(String username);

  //for the syntax, this helps: http://en.wikibooks.org/wiki/Java_Persistence/Querying#JPQL
  @Query(value = "SELECT u from User u JOIN u.userNeeds n where n.uri = ?1")
  public User findByNeedUri(URI needUri);

}
