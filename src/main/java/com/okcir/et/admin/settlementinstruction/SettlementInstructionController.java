package com.okcir.et.admin.settlementinstruction;

import com.okcir.et.admin.common.ApiResponse;
import com.okcir.et.admin.settlementinstruction.dto.SettlementInstructionRequestDto;
import com.okcir.et.admin.settlementinstruction.dto.SettlementInstructionResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/settlement-instructions")
@RequiredArgsConstructor
public class SettlementInstructionController {

  private final SettlementInstructionService settlementInstructionService;

  @PostMapping
  public ResponseEntity<ApiResponse<SettlementInstructionResponseDto>> createSettlementInstruction(
      @Valid @RequestBody SettlementInstructionRequestDto request) {
    SettlementInstructionResponseDto created = settlementInstructionService.createSettlementInstruction(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.created(created));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<SettlementInstructionResponseDto>>> getAllSettlementInstructions(
      @RequestParam(required = false) Long accountId) {
    List<SettlementInstructionResponseDto> instructions = settlementInstructionService.getAllSettlementInstructions(accountId);
    return ResponseEntity.ok(ApiResponse.success(instructions));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<SettlementInstructionResponseDto>> getSettlementInstructionById(@PathVariable Long id) {
    SettlementInstructionResponseDto instruction = settlementInstructionService.getSettlementInstructionById(id);
    return ResponseEntity.ok(ApiResponse.success(instruction));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<SettlementInstructionResponseDto>> updateSettlementInstruction(
      @PathVariable Long id,
      @Valid @RequestBody SettlementInstructionRequestDto request) {
    SettlementInstructionResponseDto updated = settlementInstructionService.updateSettlementInstruction(id, request);
    return ResponseEntity.ok(ApiResponse.success("Settlement instruction updated successfully", updated));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteSettlementInstruction(@PathVariable Long id) {
    settlementInstructionService.deleteSettlementInstruction(id);
    return ResponseEntity.ok(ApiResponse.success("Settlement instruction deleted successfully", null));
  }
}
