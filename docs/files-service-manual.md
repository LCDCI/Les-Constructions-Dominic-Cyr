# Files Service – Frontend Integration Guide

This guide explains how to integrate the Files Service into the React.js frontend application. It includes step-by-step instructions, reusable React components, upload/download/delete examples, and a troubleshooting FAQ. This is a document that may have errors, so please do not hesitate to contact @erickossovsky for changes.

---

# 1. API Overview

| Method | Endpoint                         | Description                             |
|--------|----------------------------------|-----------------------------------------|
| POST   | /files                           | Uploads a file (multipart/form-data)    |
| GET    | /files/:id                       | Downloads or displays a file            |
| DELETE | /files/:id                       | Deletes a file                           |
| GET    | /projects/:projectId/files       | Lists photos belonging to a project     |

Your React frontend will communicate only through these URLs.

---

# 2. How File Upload Works in React

The backend expects:

| Field        | Description |
|-------------|-------------|
| **file**     | The actual file uploaded from user input |
| **category** | PHOTO, DOCUMENT, or OTHER |
| **projectId** | Required for project documents/photos |
| **uploadedBy** | User ID or email |

React must send these values using a **FormData** object.

---

# 3. React Example – File Upload Component

This is a complete, ready-to-use Upload component.

```jsx
import React, { useState } from "react";

export default function FileUpload() {
  const [file, setFile] = useState(null);
  const [category, setCategory] = useState("PHOTO");
  const [projectId, setProjectId] = useState("");
  const [uploadedBy, setUploadedBy] = useState("user123");
  const [status, setStatus] = useState("");

  const handleUpload = async () => {
    if (!file) {
      setStatus("Please select a file first.");
      return;
    }

    const formData = new FormData();
    formData.append("file", file);
    formData.append("category", category);
    formData.append("projectId", projectId);
    formData.append("uploadedBy", uploadedBy);

    try {
      const res = await fetch("http://localhost:8080/files", {
        method: "POST",
        body: formData,
      });

      if (!res.ok) {
        throw new Error("Upload failed");
      }

      const data = await res.json();
      setStatus("File uploaded successfully: " + data.fileId);
    } catch (err) {
      setStatus("Error: " + err.message);
    }
  };

  return (
    <div style={{ padding: 20 }}>
      <h2>Upload File</h2>

      <input type="file" onChange={(e) => setFile(e.target.files[0])} />

      <div>
        <label>Category: </label>
        <select value={category} onChange={(e) => setCategory(e.target.value)}>
          <option value="PHOTO">PHOTO</option>
          <option value="DOCUMENT">DOCUMENT</option>
          <option value="OTHER">OTHER</option>
        </select>
      </div>

      <div>
        <label>Project ID: </label>
        <input
          type="text"
          value={projectId}
          onChange={(e) => setProjectId(e.target.value)}
        />
      </div>

      <button onClick={handleUpload}>Upload</button>

      <p>{status}</p>
    </div>
  );
}
```

---

# 4. Downloading Files in React

Files can be images, PDFs, docs, etc.

## Option A: Display File in Browser (images)

```jsx
<img src={`http://localhost:8080/files/${fileId}`} alt="preview" />
```

## Option B: Download File to the User’s Computer

```jsx
async function downloadFile(fileId) {
  const response = await fetch(`http://localhost:8080/files/${fileId}`);

  const blob = await response.blob();
  const url = window.URL.createObjectURL(blob);

  const link = document.createElement("a");
  link.href = url;
  link.download = "file"; // backend provides correct type
  link.click();
}
```

---

# 5. Listing Files for a Project (React)

```jsx
import React, { useEffect, useState } from "react";

export default function ProjectFiles({ projectId }) {
  const [files, setFiles] = useState([]);

  useEffect(() => {
    fetch(`http://localhost:8080/projects/${projectId}/files`)
      .then((res) => res.json())
      .then((data) => setFiles(data))
      .catch((err) => console.error(err));
  }, [projectId]);

  return (
    <div>
      <h2>Project Files</h2>
      <div style={{ display: "flex", gap: 20, flexWrap: "wrap" }}>
        {files.map((f) => (
          <div key={f.id}>
            <img
              src={`http://localhost:8080/files/${f.id}`}
              alt={f.fileName}
              width={150}
              height={150}
              style={{ objectFit: "cover", borderRadius: 10 }}
            />
            <p>{f.fileName}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
```

---

# 6. Deleting a File

```jsx
async function deleteFile(fileId) {
  const response = await fetch(`http://localhost:8080/files/${fileId}`, {
    method: "DELETE",
  });

  if (!response.ok) {
    throw new Error("Failed to delete file");
  }

  return true;
}
```

---

# 7. Curl Commands (for debugging)

### Upload
```
curl -X POST http://localhost:8080/files \
  -F "file=@./example.png" \
  -F "category=PHOTO" \
  -F "projectId=123" \
  -F "uploadedBy=user123"
```

### Download
```
curl http://localhost:8080/files/<FILE_ID> --output downloaded-file
```

### List project files
```
curl http://localhost:8080/projects/123/files
```

### Delete
```
curl -X DELETE http://localhost:8080/files/<FILE_ID>
```

---

# 8. Allowed File Types

### Images  
- image/png  
- image/jpeg  
- image/webp  

### Documents  
- application/pdf  
- text/plain  
- application/json  
- .docx  
- .xlsx  
- application/octet-stream  

---

# 9. FAQ – Common Issues & Fixes

### 1. Upload request returns: *“415 Unsupported Media Type”*
**Cause:** You're not using `FormData`.  
**Fix:** Ensure you NEVER set `"Content-Type"` manually when sending FormData.

Correct:
```js
body: formData
```

Incorrect:
```js
headers: { "Content-Type": "multipart/form-data" }
```

---

### 2. Downloaded file is corrupted
**Cause:** You parsed the file as JSON accidentally.  
**Fix:**
```js
const blob = await response.blob();
```

---

### 3. Images won’t load in `<img>` tags
**Cause:** Missing backend CORS config.  
**Fix:** Ensure backend has:
```
Access-Control-Allow-Origin: *
```

---

### 4. “Failed to fetch” on upload
**Possible causes:**
- Backend not running  
- Wrong port  
- MINIO offline  
- File too large  

Check DevTools → Network → Request → Response.

---

### 5. List API returns an empty array
**Cause:** File category is not PHOTO.  
**Fix:** Only PHOTO files are stored per-project for gallery listings.

---

### 6. Delete works but page does not refresh
**Fix:**
Manually reload file list:

```js
setFiles(files.filter((f) => f.id !== fileId));
```


---

P.S: This document has been completed with the assistance of AI (ChatGPT Model GPT-5) for document structure, and orthograph correction.

# YOU MAY KNOW USE THE FILES SERVICE!
Author: @erickossovsky