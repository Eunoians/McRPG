package us.eunoians.mcrpg.quest.board.template;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import us.eunoians.mcrpg.quest.board.template.condition.TemplateCondition;
import us.eunoians.mcrpg.quest.definition.PhaseCompletionMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@DisplayName("TemplatePhaseDefinition")
class TemplatePhaseDefinitionTest {

    private static TemplateStageDefinition mockStage() {
        return mock(TemplateStageDefinition.class);
    }

    @Nested
    @DisplayName("canonical constructor")
    class CanonicalConstructor {

        @ParameterizedTest
        @EnumSource(PhaseCompletionMode.class)
        @DisplayName("stores the completion mode for all enum values")
        void completionMode_storedCorrectly(PhaseCompletionMode mode) {
            var phase = new TemplatePhaseDefinition(mode, List.of(), null);
            assertEquals(mode, phase.completionMode());
        }

        @Test
        @DisplayName("stores the stages list")
        void stages_storedCorrectly() {
            TemplateStageDefinition stage = mockStage();
            var phase = new TemplatePhaseDefinition(PhaseCompletionMode.ALL, List.of(stage), null);
            assertEquals(1, phase.stages().size());
            assertEquals(stage, phase.stages().get(0));
        }

        @Test
        @DisplayName("stores the condition when provided")
        void condition_storedCorrectly() {
            TemplateCondition condition = mock(TemplateCondition.class);
            var phase = new TemplatePhaseDefinition(PhaseCompletionMode.ALL, List.of(), condition);
            assertEquals(condition, phase.condition());
        }
    }

    @Nested
    @DisplayName("stages defensive copy")
    class StagesDefensiveCopy {

        @Test
        @DisplayName("mutation of original list does not affect the stored stages")
        void constructor_copiesList_externalMutationHasNoEffect() {
            TemplateStageDefinition stage = mockStage();
            ArrayList<TemplateStageDefinition> mutableList = new ArrayList<>();
            mutableList.add(stage);

            var phase = new TemplatePhaseDefinition(PhaseCompletionMode.ALL, mutableList, null);
            mutableList.clear();

            assertEquals(1, phase.stages().size());
        }

        @Test
        @DisplayName("returned stages list is unmodifiable")
        void stages_returnsUnmodifiableList() {
            var phase = new TemplatePhaseDefinition(PhaseCompletionMode.ALL, List.of(mockStage()), null);
            assertThrows(UnsupportedOperationException.class, () -> phase.stages().add(mockStage()));
        }
    }

    @Nested
    @DisplayName("getCondition")
    class GetCondition {

        @Test
        @DisplayName("returns empty when condition is null")
        void getCondition_returnsEmpty_whenNull() {
            var phase = new TemplatePhaseDefinition(PhaseCompletionMode.ALL, List.of(), null);
            assertEquals(Optional.empty(), phase.getCondition());
        }

        @Test
        @DisplayName("returns the condition wrapped in Optional when present")
        void getCondition_returnsCondition_whenPresent() {
            TemplateCondition condition = mock(TemplateCondition.class);
            var phase = new TemplatePhaseDefinition(PhaseCompletionMode.ALL, List.of(), condition);
            assertTrue(phase.getCondition().isPresent());
            assertEquals(condition, phase.getCondition().get());
        }
    }

    @Nested
    @DisplayName("backward-compatible two-arg constructor")
    class TwoArgConstructor {

        @Test
        @DisplayName("sets condition to null")
        void twoArgConstructor_setsConditionToNull() {
            var phase = new TemplatePhaseDefinition(PhaseCompletionMode.ANY, List.of());
            assertTrue(phase.getCondition().isEmpty());
        }

        @Test
        @DisplayName("preserves completion mode and stages")
        void twoArgConstructor_preservesFields() {
            TemplateStageDefinition stage = mockStage();
            var phase = new TemplatePhaseDefinition(PhaseCompletionMode.ANY, List.of(stage));
            assertEquals(PhaseCompletionMode.ANY, phase.completionMode());
            assertEquals(1, phase.stages().size());
        }
    }

    @Nested
    @DisplayName("withStages")
    class WithStages {

        @Test
        @DisplayName("returns a new instance with the updated stages")
        void withStages_returnsNewInstanceWithUpdatedStages() {
            TemplateStageDefinition original = mockStage();
            TemplateStageDefinition replacement = mockStage();
            TemplateCondition condition = mock(TemplateCondition.class);

            var phase = new TemplatePhaseDefinition(PhaseCompletionMode.ALL, List.of(original), condition);
            var updated = phase.withStages(List.of(replacement));

            assertEquals(1, updated.stages().size());
            assertEquals(replacement, updated.stages().get(0));
        }

        @Test
        @DisplayName("preserves completion mode and condition from the original")
        void withStages_preservesOtherFields() {
            TemplateCondition condition = mock(TemplateCondition.class);
            var phase = new TemplatePhaseDefinition(PhaseCompletionMode.ANY, List.of(mockStage()), condition);

            var updated = phase.withStages(List.of());

            assertEquals(PhaseCompletionMode.ANY, updated.completionMode());
            assertTrue(updated.getCondition().isPresent());
            assertEquals(condition, updated.getCondition().get());
        }

        @Test
        @DisplayName("does not modify the original instance")
        void withStages_doesNotModifyOriginal() {
            TemplateStageDefinition original = mockStage();
            var phase = new TemplatePhaseDefinition(PhaseCompletionMode.ALL, List.of(original), null);

            phase.withStages(List.of());

            assertEquals(1, phase.stages().size());
            assertEquals(original, phase.stages().get(0));
        }
    }

    @Nested
    @DisplayName("edge cases")
    class EdgeCases {

        @Test
        @DisplayName("empty stages list is valid")
        void emptyStages_isValid() {
            var phase = new TemplatePhaseDefinition(PhaseCompletionMode.ALL, List.of(), null);
            assertTrue(phase.stages().isEmpty());
        }

        @Test
        @DisplayName("null stages list throws NullPointerException")
        void nullStages_throwsNullPointerException() {
            assertThrows(NullPointerException.class,
                    () -> new TemplatePhaseDefinition(PhaseCompletionMode.ALL, null, null));
        }
    }
}
