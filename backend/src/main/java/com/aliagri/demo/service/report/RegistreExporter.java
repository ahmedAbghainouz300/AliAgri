package com.aliagri.demo.service.report;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public interface RegistreExporter {
    ByteArrayInputStream generate() throws IOException;
}