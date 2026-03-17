def convert():
    import traceback
    with open('d:/sab/swd/capstone-project-registration-tool/backend/run_app.log', 'rb') as f:
        content = f.read()
    
    for enc in ['utf-8', 'utf-16', 'utf-16-le', 'utf-16-be', 'cp1252']:
        try:
            text = content.decode(enc)
            with open('d:/sab/swd/capstone-project-registration-tool/backend/readable_run.log', 'w', encoding='utf-8') as f2:
                f2.write(text)
            print(f"Success with {enc}")
            return
        except Exception as e:
            print(f"Failed with {enc}: {e}")
    print("Could not decode log file with any encoding.")

convert()
