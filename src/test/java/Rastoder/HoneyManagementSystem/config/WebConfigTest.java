package Rastoder.HoneyManagementSystem.config;

import Rastoder.HoneyManagementSystem.controller.ExpenseController;
import Rastoder.HoneyManagementSystem.service.ExpenseService;
import Rastoder.HoneyManagementSystem.service.FileStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CORS: simulates the browser's "preflight" request that Angular triggers
 * before every non-simple request (PATCH, DELETE, JSON POST, ...).
 * Uploads: files written by FileStorageService must be served back by WebConfig.
 */
@WebMvcTest(ExpenseController.class)
@Import(FileStorageService.class)
class WebConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FileStorageService fileStorageService;

    @Test
    @DisplayName("Uploaded receipts and images are served from the folder they were stored in")
    void uploadedFiles_ShouldBeServed() throws Exception {
        String pdfUrl = fileStorageService.saveReceipt(
                new MockMultipartFile("file", "r.pdf", "application/pdf", "%PDF-1.4 test".getBytes()));
        String imageUrl = fileStorageService.saveImage(new MockMultipartFile("file", "h.png", "image/png",
                new byte[]{(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0, 0, 0}));

        try {
            mockMvc.perform(get(pdfUrl))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", "application/pdf"));
            mockMvc.perform(get(imageUrl))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", "image/png"));
        } finally {
            fileStorageService.deleteFile(pdfUrl);
            fileStorageService.deleteFile(imageUrl);
        }
    }

    @MockitoBean
    private ExpenseService expenseService;

    @Test
    @DisplayName("Preflight from the Angular dev server should allow PATCH")
    void preflight_FromFrontendOrigin_ShouldAllowPatch() throws Exception {
        mockMvc.perform(options("/api/v1/expenses/123")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "PATCH"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"))
                .andExpect(header().string("Access-Control-Allow-Methods",
                        org.hamcrest.Matchers.containsString("PATCH")));
    }

    @Test
    @DisplayName("Preflight from the native iOS app (capacitor://localhost) should be allowed")
    void preflight_FromCapacitorApp_ShouldBeAllowed() throws Exception {
        mockMvc.perform(options("/api/v1/expenses")
                        .header("Origin", "capacitor://localhost")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "capacitor://localhost"));
    }

    @Test
    @DisplayName("Preflight from an unknown origin should be rejected")
    void preflight_FromUnknownOrigin_ShouldBeForbidden() throws Exception {
        mockMvc.perform(options("/api/v1/expenses")
                        .header("Origin", "https://evil.example.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }
}
