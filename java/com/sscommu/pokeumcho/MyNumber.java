package com.sscommu.pokeumcho;

public class MyNumber {

    private int mNumber;

    /** Constructor */
    public MyNumber(int number) {
        mNumber = number;
    }

    /** Getter & Setter */
    public int getNumber() { return mNumber; }
    public void setNumber(int number) { mNumber = number; }


    public String getKM_Advanced() {

        if (mNumber < 0) {
            MyNumber minusNumber = new MyNumber(-mNumber);
            return "-" + minusNumber.getKM();
        } else {
            return getKM();
        }
    }

    public String getKM() {
        final int UNIT = 1000;
        final int K_UNIT = (int)Math.pow(UNIT, 2);
        final int M_UNIT = (int)Math.pow(UNIT, 3);

        // 값이 중간에 변경되지 않도록 임시 변수로 진행한다.
        int num = mNumber;

        if (num < UNIT) {
            return String.valueOf(num);
        } else if (num < K_UNIT) {

            // 두 자리 정수까지는 소수점 아래 첫째 자리까지도 표현할 것이기 때문에
            // 10을 먼저 곱한 후 1000으로 나누어서 불필요한 반올림을 방지한다.
            if (num < (K_UNIT / 10)) {
                num = num * 10;
                num = num / UNIT;

                StringBuilder sb = new StringBuilder();
                sb.append((num / 10));
                int mod = num % 10; // 소수점 아래 첫 번째 수
                if (mod != 0) {
                    sb.append('.').append(mod);
                }
                sb.append('K');

                return sb.toString();
            }
            // 세 자리 정수는 소수점 아래 부분을 표현하지 않는다.
            else {
                return String.valueOf(num / UNIT) + "K";
            }
        } else if (num < M_UNIT) {

            if (num < (M_UNIT / 10)) {
                num = num * 10;
                num = num / K_UNIT;

                StringBuilder sb = new StringBuilder();
                sb.append((num / 10));
                int mod = num % 10; // 소수점 아래 첫 번째 수
                if (mod != 0) {
                    sb.append('.').append(mod);
                }
                sb.append('M');

                return sb.toString();
            } else {
                return String.valueOf(num / K_UNIT) + "M";
            }
        } else {
            return "999M";
        }
    }

    // 가격 3자리 콤마(,) 구분해서 표시한다.
    public String priceCommaFormatted() {

        StringBuilder sb = new StringBuilder();

        String priceString = String.valueOf(mNumber);
        int length = priceString.length();

        for (int i = 0; i < length; i++) {
            if (i % 3 == 0 && i != 0) {
                sb.append(',');
            }
            sb.append(priceString.charAt(length - (1 + i)));
        }

        return sb.reverse().toString();
    }
}
