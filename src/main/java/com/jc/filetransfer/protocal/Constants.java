package com.jc.filetransfer.protocal;

public enum Constants {
    SUCCESS(0), FILE_NOT_FOUND(1), NO_AVAILABEL_DISK_SPACE(2);

    private int id;

    Constants(int id) {
        this.id = id;
    }

    public static Constants parse(int id) {
        switch (id) {
            case 0:
                return SUCCESS;
            case 1:
                return FILE_NOT_FOUND;
            case 2:
                return NO_AVAILABEL_DISK_SPACE;
            default:
                throw new IllegalArgumentException("Unknown error type: " + id);
        }
    }

    public int getId() {
        return id;
    }

}
