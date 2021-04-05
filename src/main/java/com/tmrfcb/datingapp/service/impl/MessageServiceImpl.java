package com.tmrfcb.datingapp.service.impl;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.tmrfcb.datingapp.domain.Message;
import com.tmrfcb.datingapp.repository.MessageRepository;
import com.tmrfcb.datingapp.repository.search.MessageSearchRepository;
import com.tmrfcb.datingapp.service.MessageService;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Message}.
 */
@Service
@Transactional
public class MessageServiceImpl implements MessageService {

    private final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);

    private final MessageRepository messageRepository;

    private final MessageSearchRepository messageSearchRepository;

    public MessageServiceImpl(MessageRepository messageRepository, MessageSearchRepository messageSearchRepository) {
        this.messageRepository = messageRepository;
        this.messageSearchRepository = messageSearchRepository;
    }

    @Override
    public Message save(Message message) {
        log.debug("Request to save Message : {}", message);
        Message result = messageRepository.save(message);
        messageSearchRepository.save(result);
        return result;
    }

    @Override
    public Optional<Message> partialUpdate(Message message) {
        log.debug("Request to partially update Message : {}", message);

        return messageRepository
            .findById(message.getId())
            .map(
                existingMessage -> {
                    if (message.getSenderId() != null) {
                        existingMessage.setSenderId(message.getSenderId());
                    }
                    if (message.getReceiverId() != null) {
                        existingMessage.setReceiverId(message.getReceiverId());
                    }
                    if (message.getMessageContent() != null) {
                        existingMessage.setMessageContent(message.getMessageContent());
                    }
                    if (message.getMessageTitle() != null) {
                        existingMessage.setMessageTitle(message.getMessageTitle());
                    }
                    if (message.getMessageDate() != null) {
                        existingMessage.setMessageDate(message.getMessageDate());
                    }

                    return existingMessage;
                }
            )
            .map(messageRepository::save)
            .map(
                savedMessage -> {
                    messageSearchRepository.save(savedMessage);

                    return savedMessage;
                }
            );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Message> findAll(Pageable pageable) {
        log.debug("Request to get all Messages");
        return messageRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Message> findOne(Long id) {
        log.debug("Request to get Message : {}", id);
        return messageRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Message : {}", id);
        messageRepository.deleteById(id);
        messageSearchRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Message> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Messages for query {}", query);
        return messageSearchRepository.search(queryStringQuery(query), pageable);
    }
}
