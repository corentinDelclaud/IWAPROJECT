package iwaproject.transaction.repository;

import iwaproject.transaction.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<Conversation, Integer> {}