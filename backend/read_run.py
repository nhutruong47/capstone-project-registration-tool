with open('d:/sab/swd/capstone-project-registration-tool/backend/run_app.log', 'r', encoding='utf-16') as f:
    lines = f.readlines()
with open('d:/sab/swd/capstone-project-registration-tool/backend/readable_run.log', 'w', encoding='utf-8') as f:
    f.writelines(lines)
print("Log file converted successfully to readable_run.log")
