package ru.itmo.security.note;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api")
public class NoteController {

    private final NoteRepository noteRepository;

    public NoteController(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    @GetMapping("/data")
    public NotesResponse getData(Principal principal) {
        List<NoteResponse> notes = noteRepository
                .findAllByOwnerUsernameOrderById(principal.getName())
                .stream()
                .map(NoteController::toSafeResponse)
                .toList();
        return new NotesResponse(notes);
    }

    @PostMapping("/notes")
    @ResponseStatus(HttpStatus.CREATED)
    public NoteResponse createNote(
            Principal principal,
            @Valid @RequestBody CreateNoteRequest request) {
        Note saved = noteRepository.save(new Note(
                principal.getName(), request.title().trim(), request.content()));
        return toSafeResponse(saved);
    }

    private static NoteResponse toSafeResponse(Note note) {
        return new NoteResponse(
                note.getId(),
                HtmlUtils.htmlEscape(note.getTitle()),
                HtmlUtils.htmlEscape(note.getContent()));
    }

    public record CreateNoteRequest(
            @NotBlank @Size(max = 100) String title,
            @Size(max = 2000) String content) {
    }

    public record NoteResponse(Long id, String title, String content) {
    }

    public record NotesResponse(List<NoteResponse> notes) {
        public NotesResponse {
            notes = List.copyOf(notes);
        }

        @Override
        public List<NoteResponse> notes() {
            return List.copyOf(notes);
        }
    }
}
