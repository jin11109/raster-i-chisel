# Download HLS arbitrary precision types
INCLUDE_DIR := common/include/types
HLS_MARKER := $(INCLUDE_DIR)/ap_fixed.h
HLS_URL := https://github.com/Xilinx/HLS_arbitrary_Precision_Types/archive/refs/heads/master.zip
TMP_DIR := tmp_hls

.PHONY: csim cosim download_types clean

csim: download_types
	$(MAKE) -C c_sim demo

cosim: download_types
	$(MAKE) -C ../soc verilate
	$(MAKE) -C cosim cosim

# Download HLS headers
download_types:
	@if [ -f "$(HLS_MARKER)" ]; then \
		echo "[INFO] HLS headers already present. Skipping download."; \
	else \
		echo "[INFO] Downloading Xilinx HLS Arbitrary Precision Types..."; \
		mkdir -p $(TMP_DIR); \
		wget -O $(TMP_DIR)/hls.zip $(HLS_URL); \
		unzip -q $(TMP_DIR)/hls.zip -d $(TMP_DIR); \
		mkdir -p $(INCLUDE_DIR); \
		cp -r $(TMP_DIR)/HLS_arbitrary_Precision_Types-master/include/* $(INCLUDE_DIR)/; \
		rm -rf $(TMP_DIR); \
		echo "[INFO] HLS headers downloaded into $(INCLUDE_DIR)"; \
	fi

clean:
	rm -r $(INCLUDE_DIR)
	$(MAKE) -C c_sim clean
	$(MAKE) -C cosim clean