# Rendered sprite assets used in 0.11–0.12

Generated with the imagegen tool during the prior authorized development session. Source PNGs are retained in C:/Users/alica/.codex/generated_images/01a131bb-0229-71d3-ba83-0528f4c80fcc/.

- exec-b10e964f-2270-4338-b3df-9aeb3f8b1cd5.png: six color columns, locomotive/carriage rows. Compact rounded miniature trains, ivory roofs, brass fittings, fixed overhead camera, transparent background. Actual noses point down; runtime rotates each sprite by 180 degrees.
- exec-a262780f-3948-43fb-94dc-432082334cfc.png: six color columns and eight robot yaw rows, same overhead camera and matching ivory/brass toy materials. Actual row direction order is reversed on extraction.

Technical extraction: largest connected alpha components, row/column ordering, bounding-box crop with three pixels padding; trains fit within 320×480, robots within 320×224; WebP quality 95 retains transparency. The resulting 12 train and 48 robot files have the v11 suffix. No creative repainting was performed by the extraction script. Runtime Canvas lighting, shadows and palette changes produce the 0.12 scene.
